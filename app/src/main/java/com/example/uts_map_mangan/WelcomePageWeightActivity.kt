package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomePageWeightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_weight)

        // Menangani klik tombol "Create a New Account"
        val btnGoNext = findViewById<Button>(R.id.btn_go_next)
        btnGoNext.setOnClickListener {
            // Membuat Intent untuk membuka WelcomePageNameActivity
            val intent = Intent(this, WelcomePageHeightActivity::class.java)
            startActivity(intent)
        }
    }
}