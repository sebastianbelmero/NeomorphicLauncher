package com.neumorphic.launcher.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neumorphic.launcher.databinding.ItemAppBinding
import com.neumorphic.launcher.model.AppInfo

/**
 * Adapter untuk App Drawer — Daftar semua aplikasi terinstal.
 * Adapter for App Drawer — List of all installed apps.
 *
 * Mendukung klik untuk membuka aplikasi dan long-click untuk opsi tambahan.
 * Supports click to open app and long-click for additional options.
 */
class AppAdapter(
    private val onItemClick: (AppInfo) -> Unit,
    private val onItemLongClick: (AppInfo) -> Unit = {}
) : ListAdapter<AppInfo, AppAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppViewHolder(
        private val binding: ItemAppBinding,
        private val onItemClick: (AppInfo) -> Unit,
        private val onItemLongClick: (AppInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.apply {
                appIcon.setImageDrawable(app.icon)
                appLabel.text = app.label
                root.setOnClickListener { onItemClick(app) }
                root.setOnLongClickListener {
                    onItemLongClick(app)
                    true
                }
            }
        }
    }

    private class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem == newItem
    }
}
