package com.example.bookreader.bookreaderapp

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import android.app.Activity
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.Downloading
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class Application : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activityReferences = 0 // Track number of active activities

    override fun onCreate() {
        super.onCreate()
        // Registering ActivityLifecycleCallbacks to track activity lifecycle events
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                if (activityReferences == 0) {
                    // App enters foreground
                }
                activityReferences++
            }

            override fun onActivityStopped(activity: Activity) {
                activityReferences--
                if (activityReferences == 0) {
                    // All activities are stopped, meaning the app is in the background
                    cancelRunningDownloadsAndClearDatabase()
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    private fun cancelRunningDownloadsAndClearDatabase() {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        applicationScope.launch {
            val runningDownloads = getCurrentlyRunningDownloadIds()
            for (downloadId in runningDownloads.map { it.downloadId }) {
                downloadManager.remove(downloadId)
            }
            clearCurrentlyLoadingDatabase()
        }
    }

    private suspend fun clearCurrentlyLoadingDatabase() {
        val loadingDao = AppDatabase.DatabaseBuilder.getInstance(applicationContext).loadingDao()
        loadingDao.deleteAllDownloadingBooks()
    }

    private suspend fun getCurrentlyRunningDownloadIds(): List<Downloading> {
        val loadingDao = AppDatabase.DatabaseBuilder.getInstance(applicationContext).loadingDao()
        return loadingDao.getAllDownloadingBooks()
    }
}
