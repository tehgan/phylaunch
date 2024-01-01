package com.tehgan.phylaunch.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.tehgan.phylaunch.data.LauncherDao
import com.tehgan.phylaunch.data.LauncherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

// TODO: Works, but doesn't follow MVVM well.
// See;
// https://stackoverflow.com/a/59109512
// https://stackoverflow.com/questions/47610676/how-and-where-to-use-transformations-switchmap
// TODO: Also look into Flow 'Intermediaries'

// Number of cells per page (currently 20 for 4*5 grid)
private const val CELL_COUNT = 20

class LauncherViewModel(private val launcherDao: LauncherDao): ViewModel() {

    /**
     * Given a page number, fetch and return said page of apps as LiveData
     */
    fun getPage(pageNum: Int) : LiveData<List<LauncherInfo>> {
        val startNum = (pageNum * 20)
        val endNum = (startNum + 19)
        val page = launcherDao.getPage(startNum, endNum)
        return page.asLiveData()
    }

    private val _pagedAppList: MutableStateFlow<List<LauncherInfo>> = MutableStateFlow(emptyList())
    /**
     * For use in HomeAdapter. Initially empty, the value's given by calling [populateAppList()]
     * whenever [getPage()] updates; achievable via observers.
     */
    val pagedAppList get() = _pagedAppList

    // List of blank apps/cells, for use in populateAppList()
    private val blankAppList = List(20) { index -> LauncherInfo(
        position = index,
        packageName = "",
        packageLabel = ""
    ) }

    /**
     * Populate app list with blank cells and/or LauncherInfo (apps)
     */
    fun populateAppList(list: List<LauncherInfo>, pageNum: Int) {
        val modList = blankAppList.toMutableList()
        val pageMod = (CELL_COUNT * pageNum) // Subtracted to ensure the app list remains 0-19
        for (app in list) {
            modList[(app.position) - pageMod] = app
        }
        _pagedAppList.value = modList
    }

    /**
     * Returns a Flow<List<LauncherInfo>> containing all apps in the database.
     */
    private val appList = launcherDao.getAppList()

    /**
     * Searches for an app by package name, then removes from the database if found.
     */
    fun searchAndDestroy(packageName: String) {

        // Creates a Flow of apps in database that match the uninstalled package, if there are any
        val filteredApps = appList.map { app ->
            app.filter { app ->
                app.packageName.contains(packageName)
            }
        }

        // Removes all apps in database that match the uninstalled package
        viewModelScope.launch {

            filteredApps.cancellable().collect { apps ->
                apps.forEach { app ->
                    deleteApp(app)
                }

                // Exit collection; otherwise, it runs indefinitely
                this.coroutineContext.job.cancel()
            }

        }

    }

    fun addApp(
        position: Int,
        packageName: String,
        packageLabel: String,
    ) {
        val app = LauncherInfo(
            packageName = packageName,
            packageLabel = packageLabel,
            position = position
        )

        viewModelScope.launch(Dispatchers.IO) {
            launcherDao.insert(app)
        }
    }

    // Unused for now. When moving of apps is introduced, this function will be utilized.
    fun updateApp(
        position: Int,
        packageName: String,
        packageLabel: String,
    ) {
        val app = LauncherInfo(
            position = position,
            packageName = packageName,
            packageLabel = packageLabel
        )
        viewModelScope.launch(Dispatchers.IO) {
            launcherDao.update(app)
        }
    }

    fun deleteApp(app: LauncherInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            launcherDao.delete(app)
        }
    }
}

// Singleton
class LauncherViewModelFactory(private val launcherDao: LauncherDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LauncherViewModel(launcherDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}