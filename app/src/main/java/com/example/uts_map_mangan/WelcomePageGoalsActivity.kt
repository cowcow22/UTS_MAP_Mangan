package com.example.uts_map_mangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WelcomePageGoalsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_goals)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        // Menangani perubahan pemilihan RadioButton
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Temukan RadioButton yang dipilih
            val selectedButton = findViewById<RadioButton>(checkedId)
            selectedButton.setBackgroundResource(R.drawable.after_hover_green_button) // Ganti warna latar belakang

            // Reset warna latar belakang untuk RadioButton lainnya
            resetOtherButtons(checkedId)
        }

        // Menangani klik tombol "Go Next"
        val btnGoNext = findViewById<Button>(R.id.btn_go_next)
        btnGoNext.setOnClickListener {
            // Pastikan ada RadioButton yang dipilih
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                // Membuat Intent untuk membuka WelcomePageWeightActivity
                val intent = Intent(this, WelcomePageWeightActivity::class.java)
                startActivity(intent)
            } else {
                // Menampilkan pesan kesalahan jika tidak ada pilihan
                Toast.makeText(this, "Please select a goal.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetOtherButtons(selectedId: Int) {
        val weightLossButton = findViewById<RadioButton>(R.id.weightLossButton)
        val maintainWeightButton = findViewById<RadioButton>(R.id.maintainWeightButton)
        val weightGainButton = findViewById<RadioButton>(R.id.weightGainButton)

        // Kembalikan warna latar belakang default untuk RadioButton yang tidak dipilih
        if (selectedId != R.id.weightLossButton) {
            weightLossButton.setBackgroundResource(R.drawable.green_button_background)
        }
        if (selectedId != R.id.maintainWeightButton) {
            maintainWeightButton.setBackgroundResource(R.drawable.green_button_background)
        }
        if (selectedId != R.id.weightGainButton) {
            weightGainButton.setBackgroundResource(R.drawable.green_button_background)
        }
    }
}
