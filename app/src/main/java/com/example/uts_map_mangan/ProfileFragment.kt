package com.example.uts_map_mangan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    companion object {
        private const val PROFILE_SETTINGS_REQUEST_CODE = 1001
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var logoutButton: Button
    private lateinit var profileSettingsButton: Button
    private lateinit var generalSettingsButton: Button
    private lateinit var notificationSettingsButton: Button
    private lateinit var feedbackButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_page, container, false)
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
        profileImage = view.findViewById(R.id.profile_image)
        profileName = view.findViewById(R.id.profile_name)
        profileEmail = view.findViewById(R.id.profile_email)
        logoutButton = view.findViewById(R.id.logout_button)
        profileSettingsButton = view.findViewById(R.id.profile_settings_button)
        generalSettingsButton = view.findViewById(R.id.general_settings_button)
        notificationSettingsButton = view.findViewById(R.id.notification_settings_button)
        feedbackButton = view.findViewById(R.id.feedback_button)

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
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error with a toast notification
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Set email from Firebase Authentication
        profileEmail.text = user?.email


        // Handle logout
        logoutButton.setOnClickListener {
            // Clear SharedPreferences
            sharedPreferences.edit().clear().apply()
            // Revoke authentication
            firebaseAuth.signOut()
            val googleSignInClient = GoogleSignIn.getClient(
                requireActivity(),
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            )
            googleSignInClient.revokeAccess().addOnCompleteListener {
                // Redirect to MainActivity
                startActivity(Intent(activity, MainActivity::class.java))
                activity?.finish()
            }
        }

        // Handle profile settings button click
        profileSettingsButton.setOnClickListener {
            val intent = Intent(activity, ProfileSettingsActivity::class.java)
            startActivityForResult(intent, PROFILE_SETTINGS_REQUEST_CODE)
        }

        // Handle general settings button click
        generalSettingsButton.setOnClickListener {
            val intent = Intent(activity, GeneralSettingsActivity::class.java)
            startActivityForResult(intent, PROFILE_SETTINGS_REQUEST_CODE)
        }

        // Handle notification settings button click
        notificationSettingsButton.setOnClickListener {
            val intent = Intent(activity, NotificationSettingsActivity::class.java)
            startActivityForResult(intent, PROFILE_SETTINGS_REQUEST_CODE)
        }

        // Handle feedback settings button click
        feedbackButton.setOnClickListener {
            val intent = Intent(activity, FeedbackSettingsActivity::class.java)
            startActivityForResult(intent, PROFILE_SETTINGS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Refresh the data
            loadUserData()
        }
    }

    private fun loadUserData() {
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
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}