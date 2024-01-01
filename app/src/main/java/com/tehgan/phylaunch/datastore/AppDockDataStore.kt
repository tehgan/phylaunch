package com.tehgan.phylaunch.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "DockApps")

class AppDockDataStore(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val APP_1_KEY = stringPreferencesKey("app_1")
        private val APP_2_KEY = stringPreferencesKey("app_2")
        private val APP_3_KEY = stringPreferencesKey("app_3")
        private val APP_4_KEY = stringPreferencesKey("app_4")
    }

    suspend fun saveApp(pos: Int, app: String) {
        dataStore.edit { preferences ->
            when (pos) {
                0 -> preferences[APP_1_KEY] = app
                1 -> preferences[APP_2_KEY] = app
                2 -> preferences[APP_3_KEY] = app
                3 -> preferences[APP_4_KEY] = app
            }
        }
    }

    suspend fun deleteApp(pos: Int) {
        dataStore.edit { preferences ->
            when (pos) {
                0 -> preferences[APP_1_KEY] = ""
                1 -> preferences[APP_2_KEY] = ""
                2 -> preferences[APP_3_KEY] = ""
                3 -> preferences[APP_4_KEY] = ""
            }
        }
    }

    val appDockFlow: Flow<List<String>> = dataStore.data.map { preferences ->
        listOf(
            preferences[APP_1_KEY] ?: "",
            preferences[APP_2_KEY] ?: "",
            preferences[APP_3_KEY] ?: "",
            preferences[APP_4_KEY] ?: ""
        )
    }

}