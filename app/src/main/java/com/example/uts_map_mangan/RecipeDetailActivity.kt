package com.example.uts_map_mangan

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RecipeDetailActivity : AppCompatActivity() {

    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val recipeName = intent.getStringExtra("RECIPE_NAME")
        val recipeImage = intent.getIntExtra("RECIPE_IMAGE", 0)
        val recipeCalories = intent.getStringExtra("RECIPE_CALORIES")
        val recipeTime = intent.getStringExtra("RECIPE_TIME")

        val imageView: ImageView = findViewById(R.id.ivRecipeDetailImage)
        val nameView: TextView = findViewById(R.id.tvRecipeDetailName)
        val caloriesView: TextView = findViewById(R.id.tvRecipeDetailCalories)
        val timeView: TextView = findViewById(R.id.tvRecipeDetailTime)
        val btnIngredients: Button = findViewById(R.id.btnIngredients)
        val btnInstructions: Button = findViewById(R.id.btnInstructions)
        val tvIngredientsList: TextView = findViewById(R.id.tvIngredientsList)
        val tvInstructionsList: TextView = findViewById(R.id.tvInstructionsList)
        val backButton: Button = findViewById(R.id.back_button)
        val favoriteButton: ImageButton = findViewById(R.id.favorite_button)

        if (recipeName != null) {
            nameView.text = recipeName
        }

        if (recipeImage != 0) {
            imageView.setImageResource(recipeImage)
        }

        if (recipeCalories != null) {
            caloriesView.text = recipeCalories
        }

        if (recipeTime != null) {
            timeView.text = recipeTime
        }

        btnIngredients.setOnClickListener {
            btnIngredients.setBackgroundResource(R.drawable.button_background)
            btnIngredients.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnInstructions.setBackgroundResource(R.drawable.button_background_off)
            btnInstructions.setTextColor(ContextCompat.getColor(this, R.color.black))
            tvIngredientsList.visibility = TextView.VISIBLE
            tvInstructionsList.visibility = TextView.GONE
        }

        btnInstructions.setOnClickListener {
            btnInstructions.setBackgroundResource(R.drawable.button_background)
            btnInstructions.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnIngredients.setBackgroundResource(R.drawable.button_background_off)
            btnIngredients.setTextColor(ContextCompat.getColor(this, R.color.black))
            tvIngredientsList.visibility = TextView.GONE
            tvInstructionsList.visibility = TextView.VISIBLE
        }

        backButton.setOnClickListener {
            finish()
        }

        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_on)
                favoriteButton.setColorFilter(ContextCompat.getColor(this, R.color.red))
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite)
                favoriteButton.setColorFilter(ContextCompat.getColor(this, R.color.black))
            }
        }
    }
}