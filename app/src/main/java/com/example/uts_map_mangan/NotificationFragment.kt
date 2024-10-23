package com.example.uts_map_mangan

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_notification, container, false)

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

        return view
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
