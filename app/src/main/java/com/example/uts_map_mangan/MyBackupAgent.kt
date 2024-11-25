package com.example.uts_map_mangan

import android.app.backup.BackupAgentHelper
import android.app.backup.FileBackupHelper
import android.app.backup.SharedPreferencesBackupHelper

class MyBackupAgent : BackupAgentHelper() {

    override fun onCreate() {
        super.onCreate()

        // Backup SharedPreferences
        val sharedPreferencesHelper = SharedPreferencesBackupHelper(this, "UserProfile")
        addHelper("shared_prefs", sharedPreferencesHelper)

        // Backup local diary images
        val diaryImagesHelper = FileBackupHelper(this, "files/Pictures")
        addHelper("diary_images", diaryImagesHelper)
    }
}