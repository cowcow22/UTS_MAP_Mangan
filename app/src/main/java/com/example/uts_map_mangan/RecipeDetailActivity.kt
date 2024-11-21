package com.example.uts_map_mangan

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeDetailActivity : AppCompatActivity() {

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
    }
}