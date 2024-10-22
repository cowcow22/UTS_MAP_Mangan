package com.example.uts_map_mangan

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomePageHeightActivity : AppCompatActivity() {
    private lateinit var heightInput: EditText
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_height)

        user = intent.getParcelableExtra("user")!!

        heightInput = findViewById(R.id.et_input_height)

        val btnGoNext = findViewById<Button>(R.id.btn_go_next)
        btnGoNext.setOnClickListener {
            val height = heightInput.text.toString().trim()
            if (height.isNotEmpty()) {
                user = user.copy(height = height)
                val intent = Intent(this, WelcomePageGenderActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            } else {
                heightInput.error = "Height cannot be empty"
            }
        }
    }
}