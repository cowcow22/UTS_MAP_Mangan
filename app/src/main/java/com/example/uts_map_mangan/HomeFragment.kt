package com.example.uts_map_mangan

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var recyclerViewDiary: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.homepage_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize SharedPreferences
        sharedPreferences =
            requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Initialize views
        profileImage = view.findViewById(R.id.imgAvatar)
        profileName = view.findViewById(R.id.tvName)
        recyclerViewDiary = view.findViewById(R.id.recyclerViewDiary)

        // Retrieve user data from SharedPreferences
        val cachedName = sharedPreferences.getString("name", null)
        val cachedProfileUrl = sharedPreferences.getString("profileUrl", null)

        if (cachedName != null) {
            profileName.text = cachedName
        }

        if (cachedProfileUrl != null) {
            Glide.with(this).load(cachedProfileUrl).into(profileImage)
        }

        // Retrieve user data from Firestore
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            firestore.collection("Users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        val profileUrl = document.getString("profile")

                        if (!name.isNullOrEmpty()) {
                            profileName.text = name
                            sharedPreferences.edit().putString("name", name).apply()
                        }

                        if (!profileUrl.isNullOrEmpty()) {
                            Glide.with(this).load(profileUrl).into(profileImage)
                            sharedPreferences.edit().putString("profileUrl", profileUrl).apply()
                        }

                        // Check if other fields are missing
                        val goal = document.getString("goal")
                        val weight = document.getString("weight")
                        val height = document.getString("height")
                        val gender = document.getString("gender")
                        val birthDate = document.getString("birthDate")

                        if (goal.isNullOrEmpty() || weight.isNullOrEmpty() || height.isNullOrEmpty() || gender.isNullOrEmpty() || birthDate.isNullOrEmpty()) {
                            // Redirect to WelcomePageNameActivity if any field is missing
                            val intent = Intent(activity, WelcomePageNameActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error with a toast notification
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Initialize RecyclerView
        recyclerViewDiary.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewDiary.adapter = DiaryAdapter(getDiaryEntries())
    }

    // Sample Data for Diary RecyclerView
    private fun getDiaryEntries(): List<DiaryEntry> {
        return listOf(
            DiaryEntry("Asian white noodle with seafood", "120 Kcal"),
            DiaryEntry("Healthy Taco Salad", "120 Kcal")
        )
    }
}