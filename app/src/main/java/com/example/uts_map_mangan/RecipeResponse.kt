// RecipeResponse.kt
package com.example.uts_map_mangan

data class RecipeResponse(
    val recipes: List<Recipe>
)

data class Recipe(
    val title: String,
    val readyInMinutes: Int,
    val nutrition: Nutrition,
    val image: String
)

data class Nutrition(
    val nutrients: List<Nutrient>
)

data class Nutrient(
    val name: String,
    val amount: Double
)