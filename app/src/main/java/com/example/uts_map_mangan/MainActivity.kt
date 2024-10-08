package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Menangani klik tombol "Create a New Account"
        val btnCreateNewAccount = findViewById<Button>(R.id.btn_create_new_account)
        btnCreateNewAccount.setOnClickListener {
            // Membuat Intent untuk membuka WelcomePageNameActivity
            val intent = Intent(this, WelcomePageNameActivity::class.java)
            startActivity(intent)
        }
    }
}
