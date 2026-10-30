package com.neumorphic.launcher.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neumorphic.launcher.databinding.ItemDockAppBinding
import com.neumorphic.launcher.model.AppInfo

/**
 * Adapter untuk dock bawah home screen.
 * Adapter for bottom dock on home screen.
 *
 * Hanya menampilkan ikon (tanpa label) untuk 4-5 aplikasi favorit.
 * Shows only icons (no labels) for 4-5 favorite apps.
 */
class DockAdapter(
    private val onItemClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, DockAdapter.DockViewHolder>(DockDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockViewHolder {
        val binding = ItemDockAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DockViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: DockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DockViewHolder(
        private val binding: ItemDockAppBinding,
        private val onItemClick: (AppInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.apply {
                dockAppIcon.setImageDrawable(app.icon)
                root.setOnClickListener { onItemClick(app) }
            }
        }
    }

    private class DockDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem == newItem
    }
}
