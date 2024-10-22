package com.example.uts_map_mangan

import User
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomePageGenderActivity : AppCompatActivity() {
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_gender)

        user = intent.getParcelableExtra("user")!!

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)

        // Handle RadioButton selection changes
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Reset all RadioButtons
            resetRadioButtons()

            // Find the selected RadioButton
            val selectedButton = findViewById<RadioButton>(checkedId)
            when (checkedId) {
                R.id.btn_male -> {
                    selectedButton.setBackgroundResource(R.drawable.male_button_selected)
                    user = user.copy(gender = "Male") // Set gender to Male
                }

                R.id.btn_female -> {
                    selectedButton.setBackgroundResource(R.drawable.female_button_selected)
                    user = user.copy(gender = "Female") // Set gender to Female
                }
            }
        }

        // Handle "Go Next" button click
        val btnGoNext = findViewById<Button>(R.id.btn_go_next)
        btnGoNext.setOnClickListener {
            // Ensure a RadioButton is selected
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                // Create Intent to open WelcomePageBirthActivity
                val intent = Intent(this, WelcomePageBirthActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            } else {
                // Show error message if no selection
                Toast.makeText(this, "Please select a gender.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetRadioButtons() {
        val maleButton = findViewById<RadioButton>(R.id.btn_male)
        val femaleButton = findViewById<RadioButton>(R.id.btn_female)

        // Reset to default background
        maleButton.setBackgroundResource(R.drawable.ic_male)
        femaleButton.setBackgroundResource(R.drawable.ic_female)
    }
}