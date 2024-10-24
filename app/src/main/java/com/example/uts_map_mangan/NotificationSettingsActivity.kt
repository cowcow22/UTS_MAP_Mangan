package com.example.uts_map_mangan

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NotificationSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_settings) // Updated to use notification_settings.xml

        findViewById<Button>(R.id.back_button_notification).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }
}