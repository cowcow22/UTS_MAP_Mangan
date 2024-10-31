package com.example.uts_map_mangan

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class NotificationFragment : Fragment() {

    // Water Tracker Variables
    private var currentIntake = 0
    private lateinit var tvCurrentIntake: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAddWater: Button

    // Food Tracker Variables
    private var currentFoodIntake = 0
    private lateinit var tvFoodEaten: TextView
    private lateinit var progressFoodBar: ProgressBar
    private lateinit var btnAddFood: Button

    // Buttons for Info and Activity
    private lateinit var btnInfo: Button
    private lateinit var btnActivity: Button

    // Layouts
    private lateinit var waterTrackerCard: CardView
    private lateinit var foodTrackerCard: CardView

    private lateinit var bmiInfo: ConstraintLayout
    private lateinit var tvBMIValue: TextView

    private lateinit var tvIdealWeight: TextView
    private lateinit var tvHeightWeight: TextView

    // Radio Button
    private lateinit var radioGroupCalories: RadioGroup
    private lateinit var radioButtonOption1: RadioButton
    private lateinit var radioButtonOption2: RadioButton
    private lateinit var radioButtonOption3: RadioButton


    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_notification, container, false)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize buttons
        btnInfo = view.findViewById(R.id.btnInfo)
        btnActivity = view.findViewById(R.id.btnActivity)

        // Water Tracker Initialization
        tvCurrentIntake = view.findViewById(R.id.tvCurrentIntake)
        progressBar = view.findViewById(R.id.progressBar)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        progressBar.max = 8

        // Food Tracker Initialization
        tvFoodEaten = view.findViewById(R.id.tvFoodEaten)
        progressFoodBar = view.findViewById(R.id.progressFoodBar)
        btnAddFood = view.findViewById(R.id.btnFood)
        progressFoodBar.max = 3

        // Layouts
        waterTrackerCard = view.findViewById(R.id.waterTracker)
        foodTrackerCard = view.findViewById(R.id.foodTracker)

        bmiInfo = view.findViewById(R.id.tvBMIInfo)
        tvBMIValue = view.findViewById(R.id.tvBMIValue)
        tvIdealWeight = view.findViewById(R.id.tvIdealWeight)

        tvHeightWeight = view.findViewById(R.id.tvHeightWeight)

        // Initialize RadioGroup and RadioButtons
        radioGroupCalories = view.findViewById(R.id.radioGroupCalories)
        radioButtonOption1 = view.findViewById(R.id.radioButtonOption1)
        radioButtonOption2 = view.findViewById(R.id.radioButtonOption2)
        radioButtonOption3 = view.findViewById(R.id.radioButtonOption3)

        // Initially show water and food trackers, hide BMI info
        waterTrackerCard.visibility = View.VISIBLE
        foodTrackerCard.visibility = View.VISIBLE
//        bmiInfo.visibility = View.GONE

        btnInfo.setTextColor(Color.GRAY)

        // Button click listeners
        btnInfo.setOnClickListener {
            btnInfo.setTextColor(Color.WHITE)
            btnActivity.setTextColor(Color.GRAY)

            // Hide trackers and show BMI info
            waterTrackerCard.visibility = View.GONE
            foodTrackerCard.visibility = View.GONE
            toggleBMIInfo()
        }

        btnActivity.setOnClickListener {
            btnActivity.setTextColor(Color.WHITE)
            btnInfo.setTextColor(Color.GRAY)

            // Show trackers and hide BMI info
            waterTrackerCard.visibility = View.VISIBLE
            foodTrackerCard.visibility = View.VISIBLE
            bmiInfo.visibility = View.GONE
        }

        btnAddWater.setOnClickListener {
            addWater()
        }

        btnAddFood.setOnClickListener {
            addFood()
        }
        // Fetch BMI value from Firebase
        fetchValues()

        // Set RadioGroup listener
        radioGroupCalories.setOnCheckedChangeListener { group, checkedId ->
            val selectedCaloriesIntake = when (checkedId) {
                R.id.radioButtonOption1 -> radioButtonOption1.text.toString()
                    .filter { it.isDigit() }

                R.id.radioButtonOption2 -> radioButtonOption2.text.toString()
                    .filter { it.isDigit() }

                R.id.radioButtonOption3 -> radioButtonOption3.text.toString()
                    .filter { it.isDigit() }

                else -> ""
            }
            if (selectedCaloriesIntake.isNotEmpty()) {
                saveCaloriesIntake(selectedCaloriesIntake)

            }
        }

        return view
    }

    private fun saveCaloriesIntake(caloriesIntake: String) {
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            val userRef = firestore.collection("Users").document(uid)
            userRef.update("caloriesIntake", caloriesIntake)
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Calories intake updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchValues() {
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            firestore.collection("Users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userGoal = document.getString("goal")
                        val bmiValue = document.getDouble("bmi")
                        val height = document.getString("height")
                        val weight = document.getString("weight")
                        if (bmiValue != null) {
                            tvBMIValue.text = String.format("%.1f", bmiValue)
                            val (category, color) = getWeightCategory(bmiValue)
                            tvIdealWeight.text = category
                            tvIdealWeight.setTextColor(color)
                            tvBMIValue.setTextColor(color)
                        }
                        if (height != null && weight != null) {
                            tvHeightWeight.text =
                                "Height (cm): ${height}     Weight (kg): ${weight}"
                        }
                        val gender = document.getString("gender")

                        val birthDate = document.getString("birthDate")
                        val age = birthDate?.let { calculateAge(it) }


                        if (gender != null && weight != null && height != null && age != null) {
                            val bmr =
                                calculateBMR(gender, weight.toDouble(), height.toDouble(), age)
                            val recommendedCalories = calculateRecommendedCalories(bmr)
                            updateRadioButtons(userGoal ?: "", bmr)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun calculateAge(birthDateString: String): Int {
        val birthYear = birthDateString.substring(0, 4).toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return currentYear - birthYear
    }

    private fun calculateBMR(gender: String, weight: Double, height: Double, age: Int): Double {
        return if (gender == "female") {
            655 + (9.6 * weight) + (1.8 * height) - (4.7 * age)
        } else {
            66 + (13.7 * weight) + (5 * height) - (6.8 * age)
        }
    }

    private fun updateRadioButtons(goal: String, bmr: Double) {
        val recommendedCalories = calculateRecommendedCalories(bmr)
        when (goal) {
            "Weight Gain" -> {
                radioButtonOption1.text =
                    "Gain Weight: ${
                        String.format(
                            "%.0f",
                            recommendedCalories + (recommendedCalories * 0.12)
                        )
                    } Calories/day"
                radioButtonOption2.text =
                    "Gain Weight: ${
                        String.format(
                            "%.0f",
                            recommendedCalories + (recommendedCalories * 0.24)
                        )
                    } Calories/day"
                radioButtonOption3.text =
                    "Gain Weight: ${
                        String.format(
                            "%.0f",
                            recommendedCalories + (recommendedCalories * 0.48)
                        )
                    } Calories/day"
                radioButtonOption1.visibility = View.VISIBLE
                radioButtonOption2.visibility = View.VISIBLE
                radioButtonOption3.visibility = View.VISIBLE
            }

            "Weight Loss" -> {
                radioButtonOption1.text =
                    "Lose Weight: ${
                        String.format(
                            "%.0f",
                            recommendedCalories - (recommendedCalories * 0.12)
                        )
                    } Calories/day"
                radioButtonOption2.text =
                    "Lose Weight: ${
                        String.format(
                            "%.0f",
                            recommendedCalories - (recommendedCalories * 0.24)
                        )
                    } Calories/day"
                radioButtonOption3.text =
                    "Lose Weight: ${
                        String.format(
                            "%.0f",
                            recommendedCalories - (recommendedCalories * 0.48)
                        )
                    } Calories/day"
                radioButtonOption1.visibility = View.VISIBLE
                radioButtonOption2.visibility = View.VISIBLE
                radioButtonOption3.visibility = View.VISIBLE
            }

            "Maintain Weight" -> {
                radioButtonOption1.text =
                    "Maintain Weight: ${String.format("%d", recommendedCalories)} Calories/day"
                radioButtonOption1.visibility = View.VISIBLE
                radioButtonOption2.visibility = View.GONE
                radioButtonOption3.visibility = View.GONE
            }

            else -> {
                radioGroupCalories.visibility = View.GONE
            }
        }
    }

    private fun getWeightCategory(bmi: Double): Pair<String, Int> {
        return when {
            bmi < 18.5 -> "Underweight" to Color.YELLOW
            bmi in 18.5..24.9 -> "Normal weight" to Color.parseColor("#4CAF50")
            bmi in 25.0..29.9 -> "Overweight" to Color.YELLOW
            else -> "Obese" to Color.RED
        }
    }

    private fun calculateRecommendedCalories(bmr: Double): Int {
        // Example calculation based on BMR and activity level
        val activityLevel = 1.2 // Sedentary (little or no exercise)
        return (bmr * activityLevel).toInt()
    }

    private fun toggleBMIInfo() {
        if (bmiInfo.visibility == View.GONE) {
            bmiInfo.visibility = View.VISIBLE
        } else {
            bmiInfo.visibility = View.GONE
        }
    }

    private fun addWater() {
        if (currentIntake < progressBar.max) {
            currentIntake++
            tvCurrentIntake.text = "$currentIntake/8 Glasses"
            updateProgressBar()
        }
    }

    private fun updateProgressBar() {
        progressBar.progress = currentIntake
    }

    private fun addFood() {
        if (currentFoodIntake < progressFoodBar.max) {
            currentFoodIntake++
            tvFoodEaten.text = "$currentFoodIntake/3 Plates"
            updateFoodProgressBar()
        }
    }

    private fun updateFoodProgressBar() {
        progressFoodBar.progress = currentFoodIntake
    }
}
