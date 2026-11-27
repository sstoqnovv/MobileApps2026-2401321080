package com.example.maintenancelog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maintenancelog.databinding.FragmentLogListBinding
import com.example.maintenancelog.ui.LogListAdapter
import com.example.maintenancelog.ui.LogListUiState
import com.example.maintenancelog.ui.LogListViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogListFragment : Fragment(R.layout.fragment_log_list) {

    private var _binding: FragmentLogListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogListViewModel by viewModels()
    private val logAdapter = LogListAdapter { log -> navigateToLogDetail(log.id) }

    private var renderedCategories: List<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogListBinding.bind(view)

        renderedCategories = null
        binding.logRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = logAdapter
        }
        binding.searchEditText.setText(viewModel.uiState.value.searchQuery)
        binding.searchEditText.doAfterTextChanged { editable ->
            viewModel.updateSearchQuery(editable?.toString().orEmpty())
        }
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refreshLogs() }
        binding.swipeRefreshLayout.setOnChildScrollUpCallback { _, _ ->
            binding.logRecyclerView.canScrollVertically(-1)
        }
        binding.addLogFab.setOnClickListener { navigateToLogDetail(0L) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: LogListUiState) {
        binding.loadingIndicator.isVisible = state.isLoading
        binding.swipeRefreshLayout.isRefreshing = state.isSyncing
        binding.swipeRefreshLayout.isEnabled = !state.isLoading
        binding.emptyStateText.setText(
            if (state.searchQuery.isNotBlank() || state.selectedCategory != null) {
                R.string.empty_filtered_log_list
            } else {
                R.string.empty_log_list
            }
        )
        binding.emptyStateText.isVisible = !state.isLoading && state.logs.isEmpty()
        binding.logRecyclerView.isVisible = state.logs.isNotEmpty()
        logAdapter.submitList(state.logs)
        renderCategoryChips(state)

        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun renderCategoryChips(state: LogListUiState) {
        if (renderedCategories != state.categories) {
            renderedCategories = state.categories
            binding.categoryChipGroup.removeAllViews()
            binding.categoryChipGroup.addView(
                createCategoryChip(null, getString(R.string.category_all))
            )
            state.categories.forEach { category ->
                binding.categoryChipGroup.addView(createCategoryChip(category, category))
            }
        }

        (0 until binding.categoryChipGroup.childCount)
            .map { index -> binding.categoryChipGroup.getChildAt(index) as Chip }
            .firstOrNull { chip -> chip.tag == state.selectedCategory }
            ?.isChecked = true
    }

    private fun createCategoryChip(category: String?, label: String): Chip =
        (layoutInflater.inflate(
            R.layout.item_category_chip,
            binding.categoryChipGroup,
            false
        ) as Chip).apply {
            text = label
            tag = category
            setOnClickListener { viewModel.selectCategory(category) }
        }

    private fun navigateToLogDetail(logId: Long) {
        findNavController().navigate(
            R.id.action_logListFragment_to_logDetailFragment,
            bundleOf("logId" to logId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.logRecyclerView.adapter = null
        _binding = null
    }
}
