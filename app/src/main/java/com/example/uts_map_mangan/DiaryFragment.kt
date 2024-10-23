package com.example.uts_map_mangan

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DiaryFragment : Fragment() {

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

        // Initialize SharedPreferences
        sharedPreferences =
            requireActivity().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

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

        // INI BUAT KALENDER
        // Set the date text appearance to always be black
        calendarView.setDateTextAppearance(R.style.CustomCalendarView)

        // Default CalendarView disembunyikan
        calendarView.visibility = View.GONE

        // Set tanggal default ke hari ini
        val currentDate = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        tvSelectedDate.text = currentDate

        // Update tanggal saat tanggal di CalendarView berubah
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = java.text.SimpleDateFormat("EEEE, dd MMMM yyyy", java.util.Locale.getDefault()).format(calendar.time)
            tvSelectedDate.text = selectedDate
        }

        // Mengatur aksi tombol Show Less / Show More
        btnShowMore.setOnClickListener {
            if (!isCalendarExpanded) {
                // Menampilkan CalendarView dan menyembunyikan TextView
                calendarView.visibility = View.VISIBLE // Show CalendarView
                tvSelectedDate.visibility = View.GONE // Keep this as GONE
                btnShowMore.text = "Show Less" // Update button text

                // Update button position constraints
                val params = btnShowMore.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = calendarView.id // Set btnShowMore to be below calendarView
                btnShowMore.layoutParams = params
            } else {
                // Menyembunyikan CalendarView dan menampilkan TextView
                calendarView.visibility = View.GONE // Hide CalendarView
                tvSelectedDate.visibility = View.VISIBLE // Show TextView
                btnShowMore.text = "Show More" // Update button text

                // Update button position constraints
                val params = btnShowMore.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = tvSelectedDate.id // Set btnShowMore to be below tvSelectedDate
                btnShowMore.layoutParams = params
            }
            isCalendarExpanded = !isCalendarExpanded // Toggle the boolean
        }

        // INI BUAT MEAL DAN SNACK
        // Inside your onCreateView or onViewCreated method
        val todaysMeal = view.findViewById<TextView>(R.id.TodaysMeal)
        val todaysSnack = view.findViewById<TextView>(R.id.TodaysSnack)

        // Set onClickListener for the button to show the input dialog
        btnShowInputDialog.setOnClickListener {
            showInputDialog()
        }

        // Set the initial color of Today's Snacks to gray
        todaysSnack.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

        // Set click listener for Today's Meal
        todaysMeal.setOnClickListener {
            // Change the text color of Today's Meal to black
            todaysMeal.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            // Reset the color of Today's Snacks to gray
            todaysSnack.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

            // Optionally, handle additional actions for Today's Meal click
            Toast.makeText(context, "Today's Meal clicked", Toast.LENGTH_SHORT).show()
        }

        // Set click listener for Today's Snacks
        todaysSnack.setOnClickListener {
            // Change the text color of Today's Snacks to black
            todaysSnack.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            // Reset the color of Today's Meal to gray
            todaysMeal.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))

            // Handle Today's Snacks click
            Toast.makeText(context, "Today's Snacks clicked", Toast.LENGTH_SHORT).show()
        }

    }

    // INI BUAT MEAL DAN SNACK
    private fun showInputDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_input_meal_snack, null)

        // Initialize EditText fields and RadioGroup from the dialog layout
        val editMealName: EditText = dialogView.findViewById(R.id.editMealName)
        val editCalories: EditText = dialogView.findViewById(R.id.editCalories)
        val editTime: EditText = dialogView.findViewById(R.id.editTime)
        val radioGroupMealType: RadioGroup = dialogView.findViewById(R.id.radioGroupMealType)

        // Create an AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Meal/Snack")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Validate and add data when the user clicks "Add"
                addMealOrSnack(editMealName, editCalories, editTime, radioGroupMealType)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun addMealOrSnack(
        editMealName: EditText,
        editCalories: EditText,
        editTime: EditText,
        radioGroupMealType: RadioGroup
    ) {
        val mealName = editMealName.text.toString().trim()
        val calories = editCalories.text.toString().toLongOrNull()
        val time = editTime.text.toString().trim()
        val isMeal = radioGroupMealType.checkedRadioButtonId == R.id.radioMeal

        if (mealName.isEmpty() || calories == null || time.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Your existing Firestore logic to add the meal/snack
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            val mealData = hashMapOf(
                "mealName" to mealName,
                "calories" to calories,
                "time" to time,
                "isMeal" to isMeal
            )

            firestore.collection("Users").document(uid).collection("MealsAndSnacks")
                .add(mealData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Meal/Snack added successfully", Toast.LENGTH_SHORT).show()
                    editMealName.text.clear()
                    editCalories.text.clear()
                    editTime.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error adding meal/snack: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // INI BUAT FETCH USER
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

    fun onBackPressed() {
        activity?.onBackPressed()
        activity?.overridePendingTransition(0, 0) // No transition animation when pressing back
    }
}
