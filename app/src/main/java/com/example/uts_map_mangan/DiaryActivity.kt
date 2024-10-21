package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DiaryActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var btnShowLess: Button
    private var isCalendarExpanded = true
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        calendarView = findViewById(R.id.calendarView)
        btnShowLess = findViewById(R.id.btnShowLess)

        // Mengatur aksi tombol Show Less untuk mengecilkan/memperbesar CalendarView
        btnShowLess.setOnClickListener {
            if (isCalendarExpanded) {
                // Menyembunyikan tampilan penuh CalendarView dan menampilkan hanya tanggal hari ini
                calendarView.layoutParams.height = 150 // Ukuran tinggi ringkas
                btnShowLess.text = "Show More"
            } else {
                // Mengembalikan CalendarView ke ukuran penuh
                calendarView.layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                btnShowLess.text = "Show Less"
            }
            calendarView.requestLayout()
            isCalendarExpanded = !isCalendarExpanded
        }

        // Initialize Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Set 'Diary' as the selected item
        bottomNavigation.selectedItemId = R.id.nav_diary

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomePage::class.java))
                    overridePendingTransition(0, 0) // No transition animation
                    true
                }
                R.id.nav_diary -> {
                    // Stay on the same page, no need to do anything
                    true
                }
                R.id.nav_notification -> {
                    // Handle notification click
//                    startActivity(Intent(this, HomePage::class.java))
//                    overridePendingTransition(0, 0) // No transition animation
                    true
                }
                R.id.nav_profile -> {
                    // Handle profile click
//                    startActivity(Intent(this, HomePage::class.java))
//                    overridePendingTransition(0, 0) // No transition animation
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, 0) // No transition animation when pressing back
    }
}
