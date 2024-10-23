package com.example.uts_map_mangan

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BasePage : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isSwitchingFragment = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            if (isSwitchingFragment) return@setOnNavigationItemSelectedListener false

            isSwitchingFragment = true
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle home click
                    switchFragment(HomeFragment())
                    true
                }

                R.id.nav_diary -> {
                    // Handle diary click
                    switchFragment(DiaryFragment())
                    true
                }

                R.id.nav_notification -> {
                    // Handle notification click
                    switchFragment(NotificationFragment())
                    true
                }

                R.id.nav_profile -> {
                    // Handle profile click
                    switchFragment(ProfileFragment())
                    true
                }

                else -> false
            }.also {
                handler.postDelayed({ isSwitchingFragment = false }, 500) // 500ms delay
            }
        }
        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager

        // Check if the state is saved to avoid crash during fragment transaction
        if (fragmentManager.isStateSaved) {
            isSwitchingFragment = false
            return
        }

        val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)

        // Check if the fragment is already displayed
        if (currentFragment != null && currentFragment::class == fragment::class) {
            isSwitchingFragment = false
            return
        }

        fragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commitAllowingStateLoss() // Prevent crash if state is saved
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Exit the app instead of navigating back to previous activities
        finishAffinity()
    }
}