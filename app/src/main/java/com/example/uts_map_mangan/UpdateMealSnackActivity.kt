package com.example.uts_map_mangan

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class UpdateMealSnackActivity : AppCompatActivity() {

    private lateinit var mealSnack: MealSnack
    private lateinit var nameInput: EditText
    private lateinit var caloriesInput: EditText
    private lateinit var timePickerButton: Button
    private lateinit var updateButton: Button
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var selectedTime: String = ""
    private var selectedCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_input_meal_snack)

        mealSnack = intent.getParcelableExtra("mealSnack")!!

        nameInput = findViewById(R.id.foodname_input)
        caloriesInput = findViewById(R.id.calories_input)
        timePickerButton = findViewById(R.id.time_picker_button)
        updateButton = findViewById(R.id.update_button)
        toggleGroup = findViewById(R.id.snack_meal_toggle_group)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        // Populate input fields with existing data
        nameInput.setText(mealSnack.name)
        caloriesInput.setText(mealSnack.calories.toString())
        selectedTime = mealSnack.time
        timePickerButton.text = selectedTime

        // Set up category toggle group
        when (mealSnack.category) {
            "Meal" -> toggleGroup.check(R.id.meals)
            "Snack" -> toggleGroup.check(R.id.snacks)
        }

        timePickerButton.setOnClickListener {
            showTimePicker(timePickerButton)
        }

        updateButton.setOnClickListener {
            updateMealSnack()
        }

        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
    }

    private fun showTimePicker(timePickerButton: Button) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            selectedTime = String.format(
                "%02d:%02d %s",
                if (hour % 12 == 0) 12 else hour % 12,
                minute,
                if (hour < 12) "AM" else "PM"
            )
            timePickerButton.text = selectedTime
        }

        picker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
    }

    private fun updateMealSnack() {
        val name = nameInput.text.toString()
        val calories = caloriesInput.text.toString().toInt()
        selectedCategory = when (toggleGroup.checkedButtonId) {
            R.id.meals -> "Meal"
            R.id.snacks -> "Snack"
            else -> ""
        }

        mealSnack = mealSnack.copy(name = name, calories = calories, time = selectedTime, category = selectedCategory)

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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}