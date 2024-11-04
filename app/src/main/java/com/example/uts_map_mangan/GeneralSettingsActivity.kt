package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GeneralSettingsActivity : AppCompatActivity() {

    private lateinit var notificationSwitch: Switch
    private lateinit var popUpNotification: PopUpNotification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.general_settings)

        popUpNotification = PopUpNotification(this)

        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
        findViewById<Button>(R.id.account_management_button).setOnClickListener {
            startActivity(Intent(this, AccountManagementSetting::class.java))
        }

        notificationSwitch = findViewById(R.id.notif_switch)
        updateNotificationSwitch() // Set the default value of the switch

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                popUpNotification.startNotifications()
                Toast.makeText(this, "Pop-up notifications started", Toast.LENGTH_SHORT).show()
            } else {
                popUpNotification.stopNotifications()
                Toast.makeText(this, "Pop-up notifications stopped", Toast.LENGTH_SHORT).show()
            }
            updateNotificationSwitch() // Update the switch state after changing notifications
        }
    }

    private fun updateNotificationSwitch() {
        // Check if notifications are scheduled and update the switch state
        val areNotificationsScheduled = popUpNotification.areNotificationsScheduled()
        Log.d("GeneralSettingsActivity", "Are notifications scheduled: $areNotificationsScheduled")
        notificationSwitch.isChecked = areNotificationsScheduled
    }
}