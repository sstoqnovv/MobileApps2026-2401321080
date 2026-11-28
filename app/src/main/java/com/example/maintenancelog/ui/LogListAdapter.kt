package com.example.maintenancelog.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maintenancelog.R
import com.example.maintenancelog.data.local.MaintenanceLogEntity
import com.example.maintenancelog.databinding.ItemMaintenanceLogBinding
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

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
            val context = binding.root.context
            val categoryStyle = categoryStyle(log.category)
            binding.logTitle.text = log.title
            binding.logCategory.text = log.category
            binding.logDate.text = dateFormat.format(Date(log.date))
            binding.logCost.text = currencyFormat.format(log.cost)
            binding.categoryBadge.setCardBackgroundColor(
                ContextCompat.getColor(context, categoryStyle.containerColorRes)
            )
            binding.categoryIcon.setImageResource(categoryStyle.iconRes)
            binding.categoryIcon.setColorFilter(
                ContextCompat.getColor(context, categoryStyle.contentColorRes)
            )
            binding.root.setOnClickListener { onLogClick(log) }
        }

        private fun categoryStyle(category: String): CategoryStyle =
            when (category.trim().lowercase(Locale.ROOT)) {
                "engine" -> CategoryStyle(
                    R.drawable.ic_category_engine,
                    R.color.category_engine_container,
                    R.color.category_engine_content
                )
                "oil" -> CategoryStyle(
                    R.drawable.ic_category_oil,
                    R.color.category_oil_container,
                    R.color.category_oil_content
                )
                "tires", "tyres" -> CategoryStyle(
                    R.drawable.ic_category_tires,
                    R.color.category_tires_container,
                    R.color.category_tires_content
                )
                "brakes" -> CategoryStyle(
                    R.drawable.ic_category_brakes,
                    R.color.category_brakes_container,
                    R.color.category_brakes_content
                )
                "filters", "filter" -> CategoryStyle(
                    R.drawable.ic_category_filters,
                    R.color.category_filters_container,
                    R.color.category_filters_content
                )
                "inspection" -> CategoryStyle(
                    R.drawable.ic_category_inspection,
                    R.color.category_inspection_container,
                    R.color.category_inspection_content
                )
                else -> CategoryStyle(
                    R.drawable.ic_category_other,
                    R.color.category_other_container,
                    R.color.category_other_content
                )
            }

        private data class CategoryStyle(
            @DrawableRes val iconRes: Int,
            @ColorRes val containerColorRes: Int,
            @ColorRes val contentColorRes: Int
        )
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
