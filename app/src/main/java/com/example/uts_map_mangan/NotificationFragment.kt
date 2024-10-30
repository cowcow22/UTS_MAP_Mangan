package com.example.uts_map_mangan

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    private lateinit var tvHeightWeight: TextView

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

        tvHeightWeight = view.findViewById(R.id.tvHeightWeight)

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

        return view
    }

    private fun fetchValues() {
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            firestore.collection("Users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val bmiValue = document.getDouble("bmi")
                        val height = document.getString("height")
                        val weight = document.getString("weight")
                        if (bmiValue != null) {
                            tvBMIValue.text = String.format("%.1f", bmiValue)
                        }
                        if (height != null && weight != null) {
                            tvHeightWeight.text =
                                "Height (cm): ${height}     Weight (kg): ${weight}"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
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
