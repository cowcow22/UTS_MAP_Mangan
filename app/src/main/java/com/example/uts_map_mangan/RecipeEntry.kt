package com.example.uts_map_mangan

data class RecipeEntry(
    val imageResId: Int,
    val name: String,
    val calories: String,
    val time: String,
    val imageUrl: String,
    val extendedIngredients: List<ExtendedIngredients>
)