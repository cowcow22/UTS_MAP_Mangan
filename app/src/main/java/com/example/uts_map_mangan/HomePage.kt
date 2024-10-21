package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
// Uncomment if you plan to use Firebase authentication
// import com.google.firebase.auth.FirebaseAuth

class HomePage : AppCompatActivity() {

    private lateinit var logoutBtn: Button
    private lateinit var recyclerViewDiary: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Initialize RecyclerView
        recyclerViewDiary = findViewById(R.id.recyclerViewDiary)
        recyclerViewDiary.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewDiary.adapter = DiaryAdapter(getDiaryEntries())

        // Initialize Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle home click
                    true
                }
                R.id.nav_diary -> {
                    // Handle profile click
                    startActivity(Intent(this, DiaryActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_notification -> {
                    // Handle profile click
//                    startActivity(Intent(this, ProfileActivity::class.java))
//                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    // Handle profile click
//                    startActivity(Intent(this, ProfileActivity::class.java))
//                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        // Initialize Logout Button (if you're using Firebase Auth)
        // Uncomment this if you're implementing the logout functionality
        // logoutBtn = findViewById(R.id.logout_btn)
        // logoutBtn.setOnClickListener {
        //     FirebaseAuth.getInstance().signOut()
        //     startActivity(Intent(this, MainActivity::class.java))
        //     finish()
        // }
    }

    // Sample Data for Diary RecyclerView
    private fun getDiaryEntries(): List<DiaryEntry> {
        return listOf(
            DiaryEntry("Asian white noodle with seafood", "120 Kcal"),
            DiaryEntry("Healthy Taco Salad", "120 Kcal")
        )
    }
}
