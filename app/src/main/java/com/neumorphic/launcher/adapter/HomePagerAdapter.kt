package com.neumorphic.launcher.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neumorphic.launcher.databinding.ItemHomePageBinding
import com.neumorphic.launcher.model.AppInfo

/**
 * Adapter untuk ViewPager2 home screen (multi-page).
 * Adapter for ViewPager2 home screen (multi-page).
 *
 * Setiap halaman berisi grid aplikasi yang bisa di-tap untuk dibuka.
 * Each page contains a grid of apps that can be tapped to open.
 */
class HomePagerAdapter :
    ListAdapter<List<AppInfo>, HomePagerAdapter.PageViewHolder>(PageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemHomePageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class PageViewHolder(
        private val binding: ItemHomePageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val appGridAdapter = AppGridAdapter { app ->
            try {
                app.launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                binding.root.context.startActivity(app.launchIntent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    binding.root.context,
                    "Tidak dapat membuka ${app.label}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        init {
            binding.appGrid.adapter = appGridAdapter
        }

        fun bind(apps: List<AppInfo>, pagePosition: Int) {
            appGridAdapter.submitList(apps)
        }
    }

    private class PageDiffCallback : DiffUtil.ItemCallback<List<AppInfo>>() {
        override fun areItemsTheSame(
            oldItem: List<AppInfo>,
            newItem: List<AppInfo>
        ): Boolean = oldItem.size == newItem.size

        override fun areContentsTheSame(
            oldItem: List<AppInfo>,
            newItem: List<AppInfo>
        ): Boolean = oldItem == newItem
    }
}
