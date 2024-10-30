package com.example.uts_map_mangan

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var recyclerViewDiary: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var tvTotalCalories: TextView
    private val diaryList = mutableListOf<DiaryEntryClass>()
    private var isLoading = true
    private lateinit var tvGreeting: TextView
    private lateinit var icGreeting: ImageView

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
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        icGreeting = view.findViewById(R.id.icGreeting)

        // Set greeting message and image
        val (greetingMessage, greetingImage) = getGreetingMessage()
        tvGreeting.text = greetingMessage
        icGreeting.setImageResource(greetingImage)

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
        diaryAdapter = DiaryAdapter(diaryList, isLoading)
        recyclerViewDiary.adapter = diaryAdapter

        // Fetch diary entries from Firestore
        fetchDiaryEntries()
    }

    private fun fetchDiaryEntries() {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time

        firestore.collection("meals_snacks")
            .whereEqualTo("accountId", firebaseAuth.currentUser?.uid)
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get()
            .addOnSuccessListener { result ->
                val newDiaryList = mutableListOf<DiaryEntryClass>()
                var totalCalories = 0L
                val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                for (document in result) {
                    val diary = document.toObject(DiaryEntryClass::class.java)
                    newDiaryList.add(diary)
                    totalCalories += diary.calories
                }
                // Sort the list by the time attribute
                newDiaryList.sortBy { diary ->
                    dateFormat.parse(diary.time)
                }
                tvTotalCalories.text = "Total Calories of Today: ${totalCalories} cal"
                diaryAdapter.updateList(newDiaryList)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error getting documents: ${exception.message}")
                Toast.makeText(
                    context,
                    "Error getting documents: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun getGreetingMessage(): Pair<String, Int> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning" to R.drawable.ic_morning
            in 12..16 -> "Good Afternoon" to R.drawable.ic_morning
            in 17..20 -> "Good Evening" to R.drawable.ic_night
            else -> "Good Night" to R.drawable.ic_night
        }
    }

}