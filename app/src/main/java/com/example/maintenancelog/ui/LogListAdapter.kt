package com.example.maintenancelog.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.databinding.ItemMaintenanceLogBinding
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

class LogListAdapter(
    private val onLogClick: (MaintenanceLogEntity) -> Unit
) : ListAdapter<MaintenanceLogEntity, LogListAdapter.LogViewHolder>(LogDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemMaintenanceLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding, onLogClick)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LogViewHolder(
        private val binding: ItemMaintenanceLogBinding,
        private val onLogClick: (MaintenanceLogEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat: DateFormat =
            DateFormat.getDateInstance(DateFormat.MEDIUM)
        private val currencyFormat: NumberFormat =
            NumberFormat.getCurrencyInstance()

        fun bind(log: MaintenanceLogEntity) {
            binding.logTitle.text = log.title
            binding.logCategory.text = log.category
            binding.logDate.text = dateFormat.format(Date(log.date))
            binding.logCost.text = currencyFormat.format(log.cost)
            binding.root.setOnClickListener { onLogClick(log) }
        }
    }

    private object LogDiffCallback : DiffUtil.ItemCallback<MaintenanceLogEntity>() {
        override fun areItemsTheSame(
            oldItem: MaintenanceLogEntity,
            newItem: MaintenanceLogEntity
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: MaintenanceLogEntity,
            newItem: MaintenanceLogEntity
        ): Boolean = oldItem == newItem
    }
}
