package com.example.uts_map_mangan

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WelcomePageBirthActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var datePicker: DatePicker
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_birth)

        firebaseAuth = FirebaseAuth.getInstance()
        datePicker = findViewById(R.id.datePicker)
        user = intent.getParcelableExtra("user")!!

        val finishButton = findViewById<Button>(R.id.btn_go_next)
        finishButton.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val birthDate = calendar.time

            // Format the date as a string
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val birthDateString = dateFormat.format(birthDate)

            saveUserData(user.copy(birthDate = birthDateString))
        }
    }

    private fun saveUserData(user: User) {
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val height = user.height?.toFloat() ?: 0f
        val weight = user.weight?.toFloat() ?: 0f
        val bmi = if (height != 0f) weight * 10000 / (height * height) else 0f

        val userMap = hashMapOf(
            "id" to currentUser.uid,
            "name" to user.name,
            "goal" to user.goal,
            "weight" to user.weight,
            "height" to user.height,
            "gender" to user.gender,
            "birthDate" to user.birthDate,
            "profile" to currentUser.photoUrl.toString(),
            "bmi" to bmi
        )

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Users").document(currentUser.uid)
            .set(userMap)
            .addOnSuccessListener {
                // Navigate to WelcomePageConfirmLoginActivity
                val intent = Intent(this, BasePage::class.java)
                startActivity(intent)
                finish() // Optionally finish the current activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
