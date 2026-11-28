package com.example.maintenancelog

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maintenancelog.data.preferences.ThemePreferences
import com.example.maintenancelog.databinding.FragmentLogListBinding
import com.example.maintenancelog.ui.LogListAdapter
import com.example.maintenancelog.ui.LogListUiState
import com.example.maintenancelog.ui.LogListViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogListFragment : Fragment(R.layout.fragment_log_list) {

    private var _binding: FragmentLogListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogListViewModel by viewModels()
    private val logAdapter = LogListAdapter { log -> navigateToLogDetail(log.id) }
    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()

    private lateinit var themePreferences: ThemePreferences
    private var renderedCategories: List<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogListBinding.bind(view)
        themePreferences = ThemePreferences(requireContext())

        renderedCategories = null
        binding.toolbar.inflateMenu(R.menu.menu_log_list)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_toggle_theme) {
                toggleTheme()
                true
            } else {
                false
            }
        }
        updateThemeToggleIcon()
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
        val logCount = state.logs.size
        val entries = resources.getQuantityString(
            R.plurals.log_summary_entries,
            logCount,
            logCount
        )
        binding.logSummaryCount.text = entries
        binding.logSummaryTotal.text = currencyFormat.format(state.totalCost)
        binding.summaryCard.isVisible = !state.isLoading
        val hasActiveFilters = state.searchQuery.isNotBlank() || state.selectedCategory != null
        binding.emptyStateTitle.setText(
            if (hasActiveFilters) {
                R.string.empty_state_title_filtered
            } else {
                R.string.empty_state_title_default
            }
        )
        binding.emptyStateText.setText(
            if (hasActiveFilters) {
                R.string.empty_filtered_log_list
            } else {
                R.string.empty_log_list
            }
        )
        binding.emptyStateContainer.isVisible = !state.isLoading && state.logs.isEmpty()
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

    private fun toggleTheme() {
        val useDarkTheme = !isDarkMode()
        themePreferences.saveDarkMode(useDarkTheme)
        AppCompatDelegate.setDefaultNightMode(
            if (useDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    private fun updateThemeToggleIcon() {
        val isDark = isDarkMode()
        binding.toolbar.menu.findItem(R.id.action_toggle_theme)?.apply {
            setIcon(if (isDark) R.drawable.ic_theme_light else R.drawable.ic_theme_dark)
            title = getString(
                if (isDark) R.string.switch_to_light_theme else R.string.switch_to_dark_theme
            )
            MenuItemCompat.setIconTintList(
                this,
                ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.m3_on_surface)
                )
            )
        }
    }

    private fun isDarkMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    override fun onDestroyView() {
        super.onDestroyView()
        binding.logRecyclerView.adapter = null
        _binding = null
    }
}
