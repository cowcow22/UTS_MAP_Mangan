package com.example.uts_map_mangan

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomePageWeightActivity : AppCompatActivity() {
    private lateinit var weightInput: EditText
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_weight)

        user = intent.getParcelableExtra("user")!!

        weightInput = findViewById(R.id.et_input_weight)

        val btnGoNext = findViewById<Button>(R.id.btn_go_next)
        btnGoNext.setOnClickListener {
            val weight = weightInput.text.toString().trim()
            if (weight.isNotEmpty()) {
                user = user.copy(weight = weight)
                val intent = Intent(this, WelcomePageHeightActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            } else {
                weightInput.error = "Weight cannot be empty"
            }
        }
    }
}