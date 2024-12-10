// SpoonacularApi.kt
package com.example.uts_map_mangan

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApi {
    @GET("recipes/random")
    fun getRandomRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int
    ): Call<RecipeResponse>

    @GET("recipes/complexSearch")
    fun getRecipesBySearchQuery(
        @Query("apiKey") apiKey: String,
        @Query("type") query: String,
        @Query("number") number: Int
    ): Call<RecipeResponse>
}