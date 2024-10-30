package com.example.uts_map_mangan

import java.util.Date

data class MealSnack(
    val name: String = "",
    val calories: Int = 0,
    val time: String = "",
    val category: String = "",
    val pictureUrl: String = "",
    val timestamp: Date = Date(),
    val accountId: String = "" // Add accountId field
)