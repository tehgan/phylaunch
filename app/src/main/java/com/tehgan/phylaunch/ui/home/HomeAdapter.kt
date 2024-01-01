package com.tehgan.phylaunch.ui.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tehgan.phylaunch.data.LauncherInfo
import com.tehgan.phylaunch.databinding.AppCellBinding
import com.tehgan.phylaunch.ui.HomePager

// Time to wait between boundary checks
const val DEBOUNCE_TIME = 300L

class HomeAdapter(
    private val addListener: (Int) -> Unit,
    private val delListener: (LauncherInfo) -> Unit
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    private lateinit var binding: AppCellBinding
    var callback: HomePager.PageSwitchCallback? = null

    private val TAG = "HomeAdapter"

    private val diff = AsyncListDiffer(this, DiffCallback())

    fun submitList(newList: List<LauncherInfo>) {
        diff.submitList(newList)
    }

    inner class HomeViewHolder(binding: AppCellBinding): RecyclerView.ViewHolder(binding.root) {
        val appLabel: TextView = binding.appLabel
        val appIcon: ImageView = binding.appIcon
    }

    class DiffCallback: DiffUtil.ItemCallback<LauncherInfo>() {
        override fun areItemsTheSame(oldItem: LauncherInfo, newItem: LauncherInfo): Boolean {
            return oldItem.position == newItem.position
        }

        override fun areContentsTheSame(oldItem: LauncherInfo, newItem: LauncherInfo): Boolean {
            return oldItem == newItem
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        binding = AppCellBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }

    override fun getItemCount(): Int = diff.currentList.size

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val context = holder.itemView.context

        val pm = context.packageManager

        // Force-reset/clean View
        holder.appLabel.text = ""
        holder.appIcon.setImageDrawable(null)
        holder.itemView.setOnClickListener(null)

        // 'Boundary' check
        // adapted from https://stackoverflow.com/a/56462539
        // TODO: Eventually make 'position check' code work w/ rows of various size (e.g. 5*5, 3*5)
        // Cell is either left-most or right-most
        if (position % 4 == 0 || (position + 1) % 4 == 0) {
            holder.itemView.boundCheck(position)
        }

        val app = diff.currentList[position]

        // Populated cell
        if (app.packageName != "") {

            val launchIntent: Intent? = pm.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                holder.itemView.setOnClickListener {
                    try {
                        startActivity(context, launchIntent, null)
                    } catch(e: ActivityNotFoundException) {
                        Toast.makeText(context, "App not installed?", Toast.LENGTH_SHORT).show()
                    }
                }
                holder.itemView.setOnLongClickListener {
                    delListener(app)
                    return@setOnLongClickListener true
                }

                holder.appLabel.text = app.packageLabel
                holder.appIcon.setImageDrawable(pm.getApplicationIcon(app.packageName))

            } else {
                Log.e(TAG, "Pkg ${app.packageName} has an invalid LaunchIntent! Removing...")
                delListener(app)
            }

        // Blank cell
        } else {
            holder.itemView.setOnClickListener {
                addListener(position)
            }
        }

    }

    // adapted from https://stackoverflow.com/a/56462539
    fun View.boundCheck(position: Int) {
        // Time in ms before user can re-run boundCheck, prevents spam
        val debounceTime = DEBOUNCE_TIME
        // TODO: Get parent Fragment, if next Fragment/page is 'null', return false?

        this.setOnKeyListener(object : View.OnKeyListener {
            private var lastClickTime: Long = 0

            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {

                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return false

                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (position % 4 == 0) {
                        if (keyCode == 21) { // D-Pad Left
                            lastClickTime = SystemClock.elapsedRealtime()
                            callback?.switchPage('l', position)
                            return true // key event consumed
                        }
                    }
                    if ((position + 1) % 4 == 0)  {
                        if (keyCode == 22) { // D-Pad Right
                            lastClickTime = SystemClock.elapsedRealtime()
                            callback?.switchPage('r', position)
                            return true
                        }
                    }

                }
                return false // key event consumed
            }
        })
    }

}