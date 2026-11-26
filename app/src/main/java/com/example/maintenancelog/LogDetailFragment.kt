package com.example.maintenancelog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.databinding.FragmentLogDetailBinding
import com.example.maintenancelog.ui.LogDetailUiState
import com.example.maintenancelog.ui.LogDetailViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogDetailFragment : Fragment(R.layout.fragment_log_detail) {

    private var _binding: FragmentLogDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogDetailViewModel by viewModels()

    private val logId: Long by lazy { arguments?.getLong(ARG_LOG_ID) ?: NEW_LOG_ID }
    private val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    private var selectedDateMillis: Long = startOfToday()
    private var populatedLogId: Long? = null
    private var navigatedBack = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogDetailBinding.bind(view)

        populatedLogId = null
        navigatedBack = false
        selectedDateMillis = startOfToday()
        showSelectedDate()

        binding.dateEditText.setOnClickListener { showDatePicker() }
        binding.dateInputLayout.setOnClickListener { showDatePicker() }
        binding.saveButton.setOnClickListener { saveLog() }
        binding.deleteButton.isVisible = logId != NEW_LOG_ID
        binding.deleteButton.setOnClickListener { confirmDelete() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }

        if (logId != NEW_LOG_ID && viewModel.uiState.value.log == null) {
            viewModel.loadLog(logId)
        }
    }

    private fun render(state: LogDetailUiState) {
        val isBusy = state.isLoading || state.isSaving || state.isDeleting
        binding.loadingIndicator.isVisible = isBusy
        binding.saveButton.isEnabled = !isBusy
        binding.deleteButton.isEnabled =
            logId != NEW_LOG_ID && state.log != null && !isBusy

        if (!state.isLoading) {
            state.log?.let { log ->
                if (populatedLogId != log.id) {
                    populateForm(log)
                    populatedLogId = log.id
                }
            }
        }

        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }

        if ((state.isSaved || state.isDeleted) && !navigatedBack) {
            navigatedBack = true
            findNavController().navigateUp()
        }
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_log_title)
            .setMessage(R.string.delete_log_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_confirm) { _, _ ->
                viewModel.deleteLog()
            }
            .show()
    }

    private fun populateForm(log: MaintenanceLogEntity) {
        binding.titleEditText.setText(log.title)
        binding.descriptionEditText.setText(log.description)
        binding.categoryEditText.setText(log.category)
        binding.costEditText.setText(log.cost.toString())
        selectedDateMillis = log.date
        showSelectedDate()
    }

    private fun saveLog() {
        clearFieldErrors()

        val title = binding.titleEditText.text?.toString()?.trim().orEmpty()
        val description = binding.descriptionEditText.text?.toString()?.trim().orEmpty()
        val category = binding.categoryEditText.text?.toString()?.trim().orEmpty()
        val cost = binding.costEditText.text
            ?.toString()
            ?.trim()
            ?.replace(',', '.')
            ?.toDoubleOrNull()

        var isValid = true
        if (title.isEmpty()) {
            binding.titleInputLayout.error = getString(R.string.error_log_title_required)
            isValid = false
        }
        if (category.isEmpty()) {
            binding.categoryInputLayout.error = getString(R.string.error_log_category_required)
            isValid = false
        }
        if (cost == null || cost < 0.0) {
            binding.costInputLayout.error = getString(R.string.error_log_cost_invalid)
            isValid = false
        }
        if (!isValid) {
            return
        }

        viewModel.saveLog(
            MaintenanceLogEntity(
                id = if (logId == NEW_LOG_ID) NEW_LOG_ID else logId,
                title = title,
                description = description,
                category = category,
                date = selectedDateMillis,
                cost = cost ?: return
            )
        )
    }

    private fun clearFieldErrors() {
        binding.titleInputLayout.error = null
        binding.categoryInputLayout.error = null
        binding.costInputLayout.error = null
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.log_date_label)
            .setSelection(toUtcSelection(selectedDateMillis))
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            selectedDateMillis = toLocalMidnight(utcMillis)
            showSelectedDate()
        }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun showSelectedDate() {
        binding.dateEditText.setText(dateFormat.format(Date(selectedDateMillis)))
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun toUtcSelection(localMillis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = localMillis }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH)
            )
        }.timeInMillis
    }

    private fun toLocalMidnight(utcMillis: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        return Calendar.getInstance().apply {
            clear()
            set(
                utc.get(Calendar.YEAR),
                utc.get(Calendar.MONTH),
                utc.get(Calendar.DAY_OF_MONTH)
            )
        }.timeInMillis
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val ARG_LOG_ID = "logId"
        const val NEW_LOG_ID = 0L
        const val DATE_PICKER_TAG = "log_date_picker"
    }
}
