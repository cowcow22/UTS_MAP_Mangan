package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
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

        // Menangani klik tombol "Go Next"
        val btnGoNext = findViewById<Button>(R.id.btn_go_next)
        btnGoNext.setOnClickListener {
            // Pastikan ada RadioButton yang dipilih
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                // Membuat Intent untuk membuka WelcomePageWeightActivity
                val intent = Intent(this, WelcomePageBirthActivity::class.java)
                startActivity(intent)
            } else {
                // Menampilkan pesan kesalahan jika tidak ada pilihan
                Toast.makeText(this, "Please select a gender.", Toast.LENGTH_SHORT).show()
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
