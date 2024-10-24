package com.example.uts_map_mangan

import User
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class WelcomePageNameActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_name)

        nameInput = findViewById(R.id.et_input_name)
        val btnGoNext = findViewById<Button>(R.id.btn_go_next)


        nameInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        btnGoNext.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                val user = User(name, null, null, null, null, null)
                val intent = Intent(this, WelcomePageGoalsActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            } else {
                nameInput.error = "Name cannot be empty"
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(nameInput.windowToken, 0)
    }
}