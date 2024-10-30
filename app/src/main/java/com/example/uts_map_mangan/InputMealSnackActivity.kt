package com.example.uts_map_mangan

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.uts_map_mangan.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.UUID

class InputMealSnackActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_CAMERA_PERMISSION = 100
    private var pictureUri: Uri? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_meal_snack)

        val timePickerButton = findViewById<Button>(R.id.time_picker_button)

        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.add_photo_button).setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Add Photo")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            builder.show()
        }

        timePickerButton.setOnClickListener {
            showTimePicker(timePickerButton)
        }

        findViewById<Button>(R.id.add_button).setOnClickListener {
            addMealOrSnack()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openCamera()
            } else {
                // Permission denied, show a dialog to direct the user to app settings
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Camera Permission Needed")
                builder.setMessage("This app needs the Camera permission to take photos. Please allow the permission in the app settings.")
                builder.setPositiveButton("Go to Settings") { dialog, _ ->
                    dialog.dismiss()
                    val intent =
                        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }
    }

    private fun openCamera() {
        val name = findViewById<EditText>(R.id.foodname_input).text.toString()
        val category = if (findViewById<MaterialButton>(R.id.meals).isChecked) "Meal" else "Snack"
        val imageFile = createImageFile(name, category)



        pictureUri = FileProvider.getUriForFile(
            Objects.requireNonNull(getApplicationContext()),
            BuildConfig.APPLICATION_ID + ".provider", imageFile!!
        )
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
        }
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val pickPhotoIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    findViewById<ImageView>(R.id.photo_preview).apply {
                        setImageURI(pictureUri)
                        background = null
                    }
                    findViewById<Button>(R.id.add_photo_button).text = ""
                }

                REQUEST_IMAGE_PICK -> {
                    pictureUri = data?.data
                    findViewById<ImageView>(R.id.photo_preview).apply {
                        setImageURI(pictureUri)
                        background = null
                    }
                    findViewById<Button>(R.id.add_photo_button).text = ""
                }
            }
        }
    }

    private fun createImageFile(name: String, category: String): File? {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val date = dateFormat.format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${date}_${category}_${name}",
            ".jpg",
            storageDir
        )
    }

    private fun showTimePicker(timePickerButton: Button) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            selectedTime = String.format(
                "%02d:%02d %s",
                if (hour % 12 == 0) 12 else hour % 12,
                minute,
                if (hour < 12) "AM" else "PM"
            )
            timePickerButton.text = selectedTime
        }

        picker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
    }

    private fun addMealOrSnack() {
        val name = findViewById<EditText>(R.id.foodname_input).text.toString()
        val calories = findViewById<EditText>(R.id.calories_input).text.toString().toIntOrNull()
        val time = selectedTime ?: ""
        val category = if (findViewById<MaterialButton>(R.id.meals).isChecked) "Meal" else "Snack"

        if (name.isEmpty()) {
            Toast.makeText(this, "Name must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        if (calories == null) {
            Toast.makeText(this, "Calories must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "Time must be selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Category must be selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (pictureUri == null) {
            Toast.makeText(this, "Picture must be added", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Adding diary entry...")
            setCancelable(false)
            show()
        }

        val storageRef =
            FirebaseStorage.getInstance().reference.child("diaries/${UUID.randomUUID()}")
        val uploadTask = storageRef.putFile(pictureUri!!)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val mealSnack = MealSnack(
                    name = name,
                    calories = calories,
                    time = time,
                    category = category,
                    pictureUrl = uri.toString(),
                    timestamp = Date(), // Set the current date and time
                    accountId = currentUser.uid
                )
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("meals_snacks").add(mealSnack).addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Diary entry added successfully", Toast.LENGTH_SHORT)
                            .show()
                        finish() // Finish the activity
                    } else {
                        Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }
}