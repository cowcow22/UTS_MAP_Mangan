// RecipeResponse.kt
package com.example.uts_map_mangan

data class RecipeResponse(
    val recipes: List<Recipe>
)

data class Recipe(
    val title: String,
    val readyInMinutes: Int,
    val spoonacularScore: Double,
    val image: String,
    val extendedIngredients: List<ExtendedIngredients>,
)

data class ExtendedIngredients(
    val original: String
)

