package com.tehgan.phylaunch.ui.appdrawer

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.tehgan.phylaunch.databinding.ListAddAppBinding

class AppDrawerAdapter(
private val appList: List<ApplicationInfo>
) : RecyclerView.Adapter<AppDrawerAdapter.ViewHolder>() {

    private val TAG = "AppDrawerAdapter"

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
        } catch(e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "$e when trying to load label for ${item.packageName}!")
            null
        }

        holder.appIcon.setImageDrawable(appIcon)
        holder.appLabel.text = item.loadLabel(pm)

        /* Intent was verified non-null when passing getApps() from
         *  AppDrawerFragment to AppDrawerAdapter */
        val launchIntent: Intent = pm.getLaunchIntentForPackage(item.packageName)!!
        holder.itemView.setOnClickListener {
            startActivity(holder.itemView.context, launchIntent, null)
        }
    }

}
