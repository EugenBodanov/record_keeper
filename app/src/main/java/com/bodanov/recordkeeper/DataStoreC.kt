package com.bodanov.recordkeeper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import java.util.concurrent.ConcurrentHashMap

object DataStoreC {
    private val cache = ConcurrentHashMap<String, DataStore<Preferences>>()

    fun of(appContext: Context, name: String): DataStore<Preferences> =
        cache.getOrPut(name) {
            PreferenceDataStoreFactory.create(
                produceFile = { appContext.preferencesDataStoreFile(name) }
            )
        }
}