# PhyLaunch
#### Description:
A basic Android launcher tailored to devices with physical keypads, created for my [CS50x](https://cs50.harvard.edu/x/) final project. Currently in **alpha**.

This repo is archived at the moment, as I'm currently dedicating my time to learning other programming languages and technologies. You're free to fork this code and use it however you wish, per the terms of the MIT license.

#### Features and Usage
PhyLaunch is quite barebones at this point in time, but it should be relatively stable. <sup>Hopefully.</sup>

Features include;
* Proper page switching via D-Pad (out-of-the-box, ViewPager2 doesn't support such a thing)
* Adding apps to the homescreen ('click' on a blank cell, select your desired app from the list, and voila)
* Removing apps from the homescreen ('long click' on a populated cell)
* App drawer (alphabetically sorted list of launchable apps)
* App dock; 4 apps that remain static, at the bottom of the screen, when moving between pages
* Page indicator
* Automatic removal of uninstalled apps/packages

## Project Layout

**BaseApplication.kt:** Used for creating a singleton instance of the database.

**MainActivity.kt:** Sets binding (`NavHostFragment`), hides the NavBar (as it's unnecessarily wasted screen space on most Android-based flipphones)

### broadcast:
**PackageReceiver.kt:** Handles package removal events. `PackageReceiver` is registered within the primary Fragment (`HomePager.kt`) and has a `LauncherViewModel` passed as an argument, which calls the function `searchAndDestroy(packageName)` in order to delete all instances of the uninstalled package from the database.
### data:
**LauncherDao.kt:** Contains queries for interacting with the database. Standard functions such as insert, update, and delete are included, along with `getAppList()` for fetching a list of all the applications in the database, and `getPage(p0: Int, p1: Int)` for fetching a list of applications inside of a specified range.

**LauncherDatabase.kt:** Room database builder.

**LauncherInfo.kt:** Database schema. Position, packageName (e.g., `com.android.settings`), and packageLabel (e.g., "Settings"). packageLabel isn't used for much at the moment, but eventually I'd like to implement support for renaming app cells.
### datastore:
**AppDockDataStore.kt:** Contains helper functions (saving and deleting) for the app dock, which uses a `preferencesDatastore` to save & display applications.
### model:
**LauncherViewModel.kt:** Contains functions and values used by the application, primarily to interact with the Room database/`LauncherDao`.
### ui:
**HomePager.kt:** The 'main' Fragment. Hosts the `ViewPager2` (which navigates between instances of `HomeFragment.kt`), the page indicator, the dock, and the app drawer. Also hosts `PackageReceiver.kt`, and sets the wallpaper.

**PageIndicator.kt:** 'Dot' page indicator. Creates a list of 'Dot' drawables, with unfocused dots having full opacity (255) and the focused dot (current page) being half the opacity (128). View is redrawn on page switch by calling `updateDot(page)` and invalidating the current PageIndicator view.

**appdrawer;**
 - **AppDrawerAdapter.kt:** RecyclerView Adapter for the app drawer. Displays a horizontal stacked list which contains installed (and launchable) application icons and labels.
 - **AppDrawerFragment.kt:** Fragment that hosts the app drawer. Provides the Adapter with a List\<ApplicationInfo> via `AppListUtils().getApps()`.

**edit;**
 - **AddAppDialogAdapter.kt:** RecyclerView Adapter for the 'add app' dialog Fragment. Similar to the app drawer, though taking on a 'windowed' appearance and using a callback to add the chosen app to the selected position.
 - **AddAppDialogFragment.kt:** Dialog Fragment for adding an app to the database/homescreen. Contains a variety of callbacks (home, dock, and stub) that utilize `setFragmentResult` to notify either `HomeFragment.kt` ('home', database) or `HomePager.kt` ('dock', preferencesDatastore).

**home;**
 - **HomeAdapter.kt:** RecyclerView Adapter for the 'homescreen' Fragments. Uses `AsyncListDiffer` so that I can implement partial updating via Payloads in the future, though at the moment it'd be fine using a ListAdapter. Contains 'bound checks' (for page switching), as well as application add & delete callbacks.
 - **HomeFragment.kt:** Fragment that hosts a 'homescreen' page. Contains page switching code and Observables for populating and submitting `LauncherViewModel`'s `pagedAppList` to the Adapter.

**info;**
 - **PermissionsInfoDialog.kt:** A dialog box to display when the user denies read access to external storage (which is required for reading the wallpaper, and seems to be granted by default on older Android versions). Currently unused, as it constantly pestered the user. I need to find a way to display this only once per launch.
### util:
**AppListUtils.kt:** Currently contains only the `getApps()` function, which is used by the 'add app' dialog as well as the app drawer. It fetches a list of installed applications, filters out the un-launchable applications (such as System UI), sorts alphabetically, and returns the filtered, sorted list.

## TODO:
* Implement a separate 'edit mode' for moving and deleting apps. 'static' mode should only allow focus on populated cells, and page-switching code should account for this.
* Add variable page support (currently hard-coded to 5 pages).
* Add widget support
* Polish visuals
* Write tests
