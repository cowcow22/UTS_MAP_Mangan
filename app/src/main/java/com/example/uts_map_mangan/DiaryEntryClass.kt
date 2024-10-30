package com.example.uts_map_mangan

import java.util.Date

data class DiaryEntryClass(
    val name: String = "",
    val calories: Long = 0,
    val time: String = "",
    val category: String = "",
    val pictureUrl: String = "",
    val timestamp: Date = Date(),
    val accountId: String = ""
)