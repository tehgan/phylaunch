package com.tehgan.phylaunch.ui.edit

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tehgan.phylaunch.databinding.ListAddAppBinding

class AddAppDialogAdapter(
    private val appList: List<ApplicationInfo>,
    private val callbackListener: (String) -> Unit
) : RecyclerView.Adapter<AddAppDialogAdapter.ViewHolder>() {

    class ViewHolder(binding: ListAddAppBinding) : RecyclerView.ViewHolder(binding.root) {
        val appIcon: ImageView = binding.appIcon
        val appLabel: TextView = binding.appLabel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListAddAppBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = appList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = appList[position]
        val pm = holder.itemView.context.packageManager

        val appIcon = try {
            item.loadIcon(pm)
        } catch(e: NameNotFoundException) {
            null
        }
        holder.appIcon.setImageDrawable(appIcon)
        holder.appLabel.text = item.loadLabel(pm)

        // Runs 'add app' code in parent fragment (e.g. HomeFragment/HomeAdapter)
        holder.itemView.setOnClickListener { callbackListener(item.packageName) }
    }

}