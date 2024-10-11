package com.example.uts_map_mangan

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class WelcomePageGenderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_gender)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupGender)

        // Menangani perubahan pemilihan RadioButton
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Reset semua RadioButton
            resetRadioButtons()

            // Temukan RadioButton yang dipilih
            val selectedButton = findViewById<RadioButton>(checkedId)
            when (checkedId) {
                R.id.btn_male -> {
                    selectedButton.setBackgroundResource(R.drawable.male_button_selected)
                }
                R.id.btn_female -> {
                    selectedButton.setBackgroundResource(R.drawable.female_button_selected)
                }
            }
        }
    }

    private fun resetRadioButtons() {
        val maleButton = findViewById<RadioButton>(R.id.btn_male)
        val femaleButton = findViewById<RadioButton>(R.id.btn_female)

        // Kembalikan ke latar belakang default
        maleButton.setBackgroundResource(R.drawable.ic_male)
        femaleButton.setBackgroundResource(R.drawable.ic_female)
    }
}
