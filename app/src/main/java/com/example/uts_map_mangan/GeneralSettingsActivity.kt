package com.example.uts_map_mangan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GeneralSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.general_settings) // Updated to use general_settings.xml

        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
        findViewById<Button>(R.id.account_management_button).setOnClickListener {
            startActivity(Intent(this, AccountManagementSetting::class.java))
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }
}