package com.example.uts_map_mangan

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment(), RecipesAdapter.OnItemClickListener, DiaryAdapter.OnItemClickListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var recyclerViewDiary: RecyclerView
    private lateinit var recyclerViewRecipes: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var recipesAdapter: RecipesAdapter
    private lateinit var tvTotalCalories: TextView
    private val diaryList = mutableListOf<MealSnack>()
    private val recipesList = mutableListOf<RecipeEntry>()
    private var isLoading = true
    private lateinit var tvGreeting: TextView
    private lateinit var icGreeting: ImageView
    private lateinit var popUpNotification: PopUpNotification
    private lateinit var notificationSharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.homepage_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        popUpNotification = PopUpNotification(requireContext())

        notificationSharedPreferences =
            requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        if (isFirstTimeOpeningApp()) {
            requestNotificationPermission()
            popUpNotification.startNotifications()
        }

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
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes)
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        icGreeting = view.findViewById(R.id.icGreeting)
        val tvTargetCalories: TextView = view.findViewById(R.id.tvTargetCalories)

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
                        val caloriesIntake = document.getString("caloriesIntake") ?: null

                        if (!name.isNullOrEmpty()) {
                            profileName.text = name
                            sharedPreferences.edit().putString("name", name).apply()
                        }

                        if (!profileUrl.isNullOrEmpty()) {
                            Glide.with(this).load(profileUrl).into(profileImage)
                            sharedPreferences.edit().putString("profileUrl", profileUrl).apply()
                        }

                        if (!caloriesIntake.isNullOrEmpty()) {
                            tvTargetCalories.text = "Target Calories: $caloriesIntake cal"
                        } else {
                            tvTargetCalories.text = "Select calories per day in notification page"
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

        // Initialize RecyclerView for Diary
        recyclerViewDiary.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        diaryAdapter = DiaryAdapter(this)
        recyclerViewDiary.adapter = diaryAdapter

        // Initialize RecyclerView for Recipes
        recyclerViewRecipes.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recipesAdapter = RecipesAdapter(recipesList, this)
        recyclerViewRecipes.adapter = recipesAdapter

        // Fetch diary entries from Firestore
        fetchDiaryEntries()

        // Load recipes (this is just an example, you should load actual data)
        loadRecipes()

        // Navigate to ProfileFragment on profileImage click
        profileImage.setOnClickListener {
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, ProfileFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            // Set the active icon to profile page
            val bottomNavigation: BottomNavigationView =
                requireActivity().findViewById(R.id.bottomNavigation)
            bottomNavigation.selectedItemId = R.id.nav_profile
        }
    }

    override fun onItemClick(recipe: RecipeEntry) {
        // Handle item click and navigate to the new layout
        val intent = Intent(activity, RecipeDetailActivity::class.java)
        intent.putExtra("RECIPE_NAME", recipe.name)
        intent.putExtra("RECIPE_IMAGE", recipe.imageResId)
        intent.putExtra("RECIPE_CALORIES", recipe.calories)
        intent.putExtra("RECIPE_TIME", recipe.time)
        startActivity(intent)
    }

    override fun onItemClick(mealSnack: MealSnack) {
        // Handle item click and navigate to activity_input_meal_snack for updating
        val intent = Intent(activity, InputMealSnackActivity::class.java)
        intent.putExtra("mealSnack", mealSnack)
        startActivity(intent)
    }

    private fun fetchDiaryEntries() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time

        firestore.collection("meals_snacks")
            .whereEqualTo("accountId", firebaseAuth.currentUser?.uid)
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get()
            .addOnSuccessListener { result ->
                val newDiaryList = mutableListOf<MealSnack>()
                var totalCalories = 0L
                val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                for (document in result) {
                    val diary = document.toObject(MealSnack::class.java)
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

    private fun loadRecipes() {
        // Example data, replace with actual data loading logic
        val exampleRecipes = listOf(
            RecipeEntry(
                R.drawable.food_image_example,
                "Healthy Taco Salad with fresh vegetables",
                "120 Kcal",
                "20 Min"
            ),
            RecipeEntry(R.drawable.food_image_example, "Grilled Chicken", "200 Kcal", "30 Min")
        )
        recipesList.clear()
        recipesList.addAll(exampleRecipes)
        recipesAdapter.notifyDataSetChanged()
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

    private fun isFirstTimeOpeningApp(): Boolean {
        val isFirstTime = notificationSharedPreferences.getBoolean("isFirstTimeOpeningApp", true)
        if (isFirstTime) {
            notificationSharedPreferences.edit().putBoolean("isFirstTimeOpeningApp", false).apply()
        }
        return isFirstTime
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    requireContext(),
                    "Notification permission granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Notification permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}