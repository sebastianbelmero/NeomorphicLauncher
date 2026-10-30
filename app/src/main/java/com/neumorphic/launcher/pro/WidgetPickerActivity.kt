package com.neumorphic.launcher.pro

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neumorphic.launcher.R
import com.neumorphic.launcher.pro.feature.WidgetHelper
import com.neumorphic.launcher.pro.feature.WidgetItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity untuk pilih widget yang akan dipasang di home screen.
 * Activity to select widget to place on home screen.
 *
 * Flow:
 *  1. User tap "Add Widget" di home
 *  2. WidgetPickerActivity terbuka, list semua widget tersedia
 *  3. User tap widget → allocate widget ID, bind ke provider
 *  4. Return result dengan widget ID ke MainActivity
 *  5. MainActivity tampilkan widget di home screen
 *
 * Flow:
 *  1. User taps "Add Widget" on home
 *  2. WidgetPickerActivity opens, lists all available widgets
 *  3. User taps widget → allocate widget ID, bind to provider
 *  4. Return result with widget ID to MainActivity
 *  5. MainActivity displays widget on home screen
 */
class WidgetPickerActivity : AppCompatActivity() {

    private lateinit var widgetHelper: WidgetHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_picker)

        widgetHelper = WidgetHelper(this)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        loadWidgets()
    }

    private fun loadWidgets() {
        lifecycleScope.launch {
            val widgets = withContext(Dispatchers.IO) {
                widgetHelper.listAvailableWidgets()
            }
            renderWidgets(widgets)
        }
    }

    private fun renderWidgets(widgets: List<WidgetItem>) {
        val recyclerView = findViewById<RecyclerView>(R.id.widgetRecyclerView)
        val emptyText = findViewById<TextView>(R.id.emptyText)

        if (widgets.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            return
        }

        recyclerView.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WidgetListAdapter(widgets) { widget ->
            placeWidget(widget)
        }
    }

    private fun placeWidget(widget: WidgetItem) {
        val provider = widget.providerInfo.provider
        val widgetId = widgetHelper.allocateWidgetId()

        if (widgetHelper.bindWidget(widgetId, provider)) {
            // Bind sukses, return widget ID ke caller
            val result = Intent().apply {
                putExtra(EXTRA_WIDGET_ID, widgetId)
                putExtra(EXTRA_PROVIDER_PACKAGE, provider.packageName)
                putExtra(EXTRA_PROVIDER_CLASS, provider.className)
            }
            setResult(RESULT_OK, result)
            finish()
        } else {
            // Bind gagal (butuh permission BIND_APPWIDGET via user consent)
            android.widget.Toast.makeText(
                this,
                "Tidak bisa menambah widget. Permission BIND_APPWIDGET diperlukan.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            widgetHelper.removeWidget(widgetId)
        }
    }

    companion object {
        const val EXTRA_WIDGET_ID = "extra_widget_id"
        const val EXTRA_PROVIDER_PACKAGE = "extra_provider_package"
        const val EXTRA_PROVIDER_CLASS = "extra_provider_class"
        const val REQUEST_BIND_APPWIDGET = 1001
    }

    private class WidgetListAdapter(
        val widgets: List<WidgetItem>,
        val onClick: (WidgetItem) -> Unit
    ) : RecyclerView.Adapter<WidgetListAdapter.VH>() {

        class VH(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_widget, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val widget = widgets[position]
            holder.view.findViewById<TextView>(R.id.widgetName).text = widget.label
            holder.view.findViewById<TextView>(R.id.widgetDescription).text =
                "${widget.packageName} · ${widget.minWidth}x${widget.minHeight}dp"
            widget.icon?.let {
                holder.view.findViewById<android.widget.ImageView>(R.id.widgetIcon)
                    .setImageDrawable(it)
            }
            holder.view.setOnClickListener { onClick(widget) }
        }

        override fun getItemCount(): Int = widgets.size
    }
}
