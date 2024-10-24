package com.example.uts_map_mangan

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.UUID

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var genderToggleGroup: MaterialButtonToggleGroup
    private lateinit var goalToggleGroup: MaterialButtonToggleGroup
    private lateinit var nameInput: EditText
    private lateinit var birthDateInput: EditText
    private lateinit var profileImage: ImageView
    private lateinit var uploadProgressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var sharedPreferences: SharedPreferences

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        genderToggleGroup = findViewById(R.id.gender_toggle_group)
        goalToggleGroup = findViewById(R.id.goal_toggle_group)
        nameInput = findViewById(R.id.name_input)
        birthDateInput = findViewById(R.id.birth_date_input)
        profileImage = findViewById(R.id.profile_image)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        sharedPreferences = getSharedPreferences("ProfileSettings", Context.MODE_PRIVATE)

        // Retrieve default values from Firebase
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            firestore.collection("Users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val gender = document.getString("gender")
                        val goal = document.getString("goal")
                        val name = document.getString("name")
                        val birthDate = document.getString("birthDate")
                        val profilePictureUrl = document.getString("profile")

                        // Set the default gender
                        when (gender) {
                            "Male" -> genderToggleGroup.check(R.id.gender_male)
                            "Female" -> genderToggleGroup.check(R.id.gender_female)
                        }

                        // Set the default goal
                        when (goal) {
                            "Weight Loss" -> goalToggleGroup.check(R.id.goal_weight_loss)
                            "Maintain Weight" -> goalToggleGroup.check(R.id.goal_maintain_weight)
                            "Weight Gain" -> goalToggleGroup.check(R.id.goal_weight_gain)
                        }

                        // Set the default name
                        nameInput.setText(name)

                        // Set the default birth date
                        birthDateInput.setText(birthDate)

                        // Set the default profile picture
                        profilePictureUrl?.let {
                            Glide.with(this).load(it).into(profileImage)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<Button>(R.id.change_profile_picture_button).setOnClickListener {
            openImagePicker()
        }

        // Set up DatePicker
        birthDateInput.setOnClickListener {
            showDatePickerDialog()
        }

        findViewById<Button>(R.id.save_button).setOnClickListener {
            val selectedGenderId = genderToggleGroup.checkedButtonId
            val selectedGoalId = goalToggleGroup.checkedButtonId

            if (selectedGenderId == -1 || selectedGoalId == -1) {
                Toast.makeText(this, "Please select both gender and goal", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val selectedGender = when (selectedGenderId) {
                R.id.gender_male -> "Male"
                R.id.gender_female -> "Female"
                else -> ""
            }

            val selectedGoal = when (selectedGoalId) {
                R.id.goal_weight_loss -> "Weight Loss"
                R.id.goal_maintain_weight -> "Maintain Weight"
                R.id.goal_weight_gain -> "Weight Gain"
                else -> ""
            }

            val name = nameInput.text.toString()
            val birthDate = birthDateInput.text.toString()

            // Update the selected values in the database
            val user = firebaseAuth.currentUser
            user?.uid?.let { uid ->
                val userData = hashMapOf(
                    "gender" to selectedGender,
                    "goal" to selectedGoal,
                    "name" to name,
                    "birthDate" to birthDate
                )
                firestore.collection("Users").document(uid).update(userData as Map<String, Any>)
                    .addOnSuccessListener {
                        // Cache the values in SharedPreferences
                        with(sharedPreferences.edit()) {
                            putString("gender", selectedGender)
                            putString("goal", selectedGoal)
                            putString("name", name)
                            putString("birthDate", birthDate)
                            apply()
                        }
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Adjust layout weight based on selection
        goalToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val selectedWeight = 2f
            val unselectedWeight = 1f

            val weightLossButton = findViewById<MaterialButton>(R.id.goal_weight_loss)
            val maintainWeightButton = findViewById<MaterialButton>(R.id.goal_maintain_weight)
            val weightGainButton = findViewById<MaterialButton>(R.id.goal_weight_gain)

            when (checkedId) {
                R.id.goal_weight_loss -> {
                    weightLossButton.layoutParams =
                        (weightLossButton.layoutParams as LinearLayout.LayoutParams).apply {
                            weight = if (isChecked) selectedWeight else unselectedWeight
                        }
                }

                R.id.goal_maintain_weight -> {
                    maintainWeightButton.layoutParams =
                        (maintainWeightButton.layoutParams as LinearLayout.LayoutParams).apply {
                            weight = if (isChecked) selectedWeight else unselectedWeight
                        }
                }

                R.id.goal_weight_gain -> {
                    weightGainButton.layoutParams =
                        (weightGainButton.layoutParams as LinearLayout.LayoutParams).apply {
                            weight = if (isChecked) selectedWeight else unselectedWeight
                        }
                }
            }
            // Refresh the layout to apply the changes
            weightLossButton.requestLayout()
            maintainWeightButton.requestLayout()
            weightGainButton.requestLayout()
        }

        findViewById<Button>(R.id.back_button).setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                birthDateInput.setText(selectedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let { uploadImage(it) }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        uploadProgressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            val storageRef =
                storage.reference.child("profile_pictures/$uid/${UUID.randomUUID()}.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val profilePictureUrl = uri.toString()
                        firestore.collection("Users").document(uid)
                            .update("profile", profilePictureUrl)
                            .addOnSuccessListener {
                                Glide.with(this).load(profilePictureUrl).into(profileImage)
                                uploadProgressBar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                with(sharedPreferences.edit()) {
                                    putString("profileUrl", profilePictureUrl)
                                    apply()
                                }
                                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .addOnFailureListener { e ->
                                uploadProgressBar.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    uploadProgressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }
}