package com.example.uts_map_mangan

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DiaryFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var btnShowLess: Button
    private var isCalendarExpanded = true
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        btnShowLess = view.findViewById(R.id.btnShowLess)
        profileImage = view.findViewById(R.id.imgAvatar)
        profileName = view.findViewById(R.id.tvName)

        // Set the date text appearance to always be black
        calendarView.setDateTextAppearance(R.style.CustomCalendarView)


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

    fun onBackPressed() {
        activity?.onBackPressed()
        activity?.overridePendingTransition(0, 0) // No transition animation when pressing back
    }
}
