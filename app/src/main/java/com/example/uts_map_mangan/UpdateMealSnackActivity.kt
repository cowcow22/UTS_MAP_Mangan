package com.example.uts_map_mangan

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UpdateMealSnackActivity : AppCompatActivity() {

    private lateinit var mealSnack: MealSnack
    private lateinit var nameInput: EditText
    private lateinit var caloriesInput: EditText
    private lateinit var updateButton: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_input_meal_snack)

        mealSnack = intent.getParcelableExtra("mealSnack")!!

        nameInput = findViewById(R.id.foodname_input)
        caloriesInput = findViewById(R.id.calories_input)
        updateButton = findViewById(R.id.update_button)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        // Populate input fields with existing data
        nameInput.setText(mealSnack.name)
        caloriesInput.setText(mealSnack.calories.toString())

        updateButton.setOnClickListener {
            updateMealSnack()
        }
    }

    private fun updateMealSnack() {
        val name = nameInput.text.toString()
        val calories = caloriesInput.text.toString().toInt()

        mealSnack = mealSnack.copy(name = name, calories = calories)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val mealSnackCollection = firestore.collection("meals_snacks")
            Log.d("UpdateMealSnackActivity", "Updating mealSnack with id: ${mealSnack.id}")
            mealSnackCollection.document(mealSnack.id).set(mealSnack)
                .addOnSuccessListener {
                    Toast.makeText(this, "Meal/Snack updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update meal/snack: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UpdateMealSnackActivity", "Error updating mealSnack", e)
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}