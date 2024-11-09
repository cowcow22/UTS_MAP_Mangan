package com.example.uts_map_mangan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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

class DiaryFragment : Fragment() {
    companion object {
        private const val REQUEST_CODE_INPUT_MEAL_SNACK = 1001
    }

    private lateinit var calendarView: CalendarView
    private lateinit var btnShowMore: Button
    private lateinit var tvSelectedDate: TextView
    private var isCalendarExpanded: Boolean = false
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btnShowInputDialog: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private val diaryList = mutableListOf<DiaryEntryClass>()
    private var selectedDate: String = ""
    private var selectedCategory: String = "Meal"
    private var isLoading = true
    private lateinit var tvGreeting: TextView
    private lateinit var icGreeting: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        btnShowMore = view.findViewById(R.id.btnShowMore)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        profileImage = view.findViewById(R.id.imgAvatar)
        profileName = view.findViewById(R.id.tvName)
        btnShowInputDialog = view.findViewById(R.id.btnShowInputDialog)
        recyclerView = view.findViewById(R.id.recyclerViewMeal)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        icGreeting = view.findViewById(R.id.icGreeting)

        // Set greeting message and image
        val (greetingMessage, greetingImage) = getGreetingMessage()
        tvGreeting.text = greetingMessage
        icGreeting.setImageResource(greetingImage)

        // Initialize SharedPreferences
        sharedPreferences =
            requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Retrieve user data from SharedPreferences
        val cachedName = sharedPreferences.getString("name", null)
        val cachedProfileUrl = sharedPreferences.getString("profileUrl", null)

        if (cachedName != null) {
            profileName.text = cachedName
        } else {
            fetchUserDataFromFirebase()
        }

        if (cachedProfileUrl != null) {
            Glide.with(this).load(cachedProfileUrl).into(profileImage)
        } else {
            fetchUserDataFromFirebase()
        }

        // Initialize RecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context)
        diaryAdapter = DiaryAdapter(diaryList, isLoading)
        recyclerView.adapter = diaryAdapter

        // Fetch diary entries from Firestore
        fetchDiaries()

        // Calendar setup
        calendarView.setDateTextAppearance(R.style.CustomCalendarView)
        calendarView.visibility = View.GONE
        val currentDate =
            java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())
        tvSelectedDate.text = currentDate
        selectedDate = currentDate

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDateFormatted =
                java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault())
                    .format(calendar.time)
            tvSelectedDate.text = selectedDateFormatted
            selectedDate = selectedDateFormatted
            filterDiaries()
        }

        btnShowMore.setOnClickListener {
            if (!isCalendarExpanded) {
                calendarView.visibility = View.VISIBLE
                tvSelectedDate.visibility = View.GONE
                btnShowMore.text = "Show Less"
                val params = btnShowMore.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = calendarView.id
                btnShowMore.layoutParams = params
            } else {
                calendarView.visibility = View.GONE
                tvSelectedDate.visibility = View.VISIBLE
                btnShowMore.text = "Show More"
                val params = btnShowMore.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = tvSelectedDate.id
                btnShowMore.layoutParams = params
            }
            isCalendarExpanded = !isCalendarExpanded
        }

        // Meal and Snack setup
        val todaysMeal = view.findViewById<TextView>(R.id.TodaysMeal)
        val todaysSnack = view.findViewById<TextView>(R.id.TodaysSnack)

        btnShowInputDialog.setOnClickListener {
            showInputDialog()
        }

        todaysSnack.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        todaysMeal.setOnClickListener {
            todaysMeal.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            todaysSnack.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            selectedCategory = "Meal"
            filterDiaries()
        }

        todaysSnack.setOnClickListener {
            todaysSnack.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            todaysMeal.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
            selectedCategory = "Snack"
            filterDiaries()
        }
    }

    private fun showInputDialog() {
        val intent = Intent(requireContext(), InputMealSnackActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_INPUT_MEAL_SNACK)
    }

    private fun fetchUserDataFromFirebase() {
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
                            if (isAdded) {
                                Glide.with(this).load(profileUrl).into(profileImage)
                            }
                            sharedPreferences.edit().putString("profileUrl", profileUrl).apply()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchDiaries() {
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            firestore.collection("meals_snacks")
                .whereEqualTo("accountId", uid)
                .get()
                .addOnSuccessListener { result ->
                    diaryList.clear()
                    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    for (document in result) {
                        val diary = document.toObject(DiaryEntryClass::class.java)
                        diaryList.add(diary)
                    }
                    // Sort the list by the time attribute
                    diaryList.sortBy { diary ->
                        dateFormat.parse(diary.time)
                    }
                    isLoading = false
                    filterDiaries()
                }
                .addOnFailureListener { exception ->
                    Log.w("DiaryFragment", "Error getting documents: ", exception)
                }
        }
    }

    private fun filterDiaries() {
        val filteredList = diaryList.filter { diary ->
            java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault())
                .format(diary.timestamp) == selectedDate && diary.category == selectedCategory
        }
        diaryAdapter.updateList(filteredList)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INPUT_MEAL_SNACK && resultCode == Activity.RESULT_OK) {
            // Refresh or reload the fragment
            fetchDiaries()
        }
    }

    fun onBackPressed() {
        activity?.onBackPressed()
        activity?.overridePendingTransition(0, 0)
    }
}