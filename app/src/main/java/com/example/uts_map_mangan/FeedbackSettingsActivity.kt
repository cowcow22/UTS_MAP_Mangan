package com.example.uts_map_mangan

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feedback_activity)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val feedbackRadioGroup = findViewById<RadioGroup>(R.id.feedback_radio_group)
        val textEditText = findViewById<EditText>(R.id.feedback_text)

        val submitButton = findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val selectedRadioButtonId = feedbackRadioGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
                val category = selectedRadioButton.text.toString()
                val text = textEditText.text.toString()
                if (text.isNotEmpty()) {
                    submitFeedback(category, text)
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
    }

    private fun submitFeedback(category: String, text: String) {
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            val feedback = Feedback(category, text, uid)
            firestore.collection("feedbacks")
                .add(feedback)
                .addOnSuccessListener {
                    Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error submitting feedback: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }


}