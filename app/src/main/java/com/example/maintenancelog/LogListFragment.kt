package com.example.maintenancelog

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogListFragment : Fragment(R.layout.fragment_log_list) {

    private var _binding: FragmentLogListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogListViewModel by viewModels()
    private val logAdapter = LogListAdapter { log -> navigateToLogDetail(log.id) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLogListBinding.bind(view)

        binding.logRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = logAdapter
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
        binding.emptyStateText.isVisible = !state.isLoading && state.logs.isEmpty()
        binding.logRecyclerView.isVisible = state.logs.isNotEmpty()
        logAdapter.submitList(state.logs)

        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
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
