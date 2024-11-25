package com.example.uts_map_mangan

import android.Manifest
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
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpdateMealSnackActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_AUTHORIZATION = 101
    private var pictureUri: Uri? = null
    private lateinit var mealSnack: MealSnack
    private lateinit var nameInput: EditText
    private lateinit var caloriesInput: EditText
    private lateinit var timePickerButton: Button
    private lateinit var updateButton: Button
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var selectedTime: String = ""
    private var selectedCategory: String = ""
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_input_meal_snack)

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Updating meal/snack...")
            setCancelable(false)
        }

        mealSnack = intent.getParcelableExtra("mealSnack")!!

        nameInput = findViewById(R.id.foodname_input)
        caloriesInput = findViewById(R.id.calories_input)
        timePickerButton = findViewById(R.id.time_picker_button)
        updateButton = findViewById(R.id.update_button)
        toggleGroup = findViewById(R.id.snack_meal_toggle_group)
        val addPhotoButton = findViewById<Button>(R.id.add_photo_button)
        val imageView = findViewById<ImageView>(R.id.image_view)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        // Populate input fields with existing data
        nameInput.setText(mealSnack.name)
        caloriesInput.setText(mealSnack.calories.toString())
        selectedTime = mealSnack.time
        timePickerButton.text = selectedTime

        // Set up category toggle group
        when (mealSnack.category) {
            "Meal" -> toggleGroup.check(R.id.meals)
            "Snack" -> toggleGroup.check(R.id.snacks)
        }

        // Load the image URL into the ImageView
        if (mealSnack.pictureUrl.isNotEmpty()) {
            Glide.with(this)
                .load(mealSnack.pictureUrl)
                .into(imageView)
        }

        timePickerButton.setOnClickListener {
            showTimePicker(timePickerButton)
        }

        updateButton.setOnClickListener {
            updateMealSnack()
        }

        addPhotoButton.setOnClickListener {
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

        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
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
            this,
            BuildConfig.APPLICATION_ID + ".provider",
            imageFile
        )
        Log.d("SavedFileFromCamera", "Saved file path: ${pictureUri}")

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
        }
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun createImageFile(name: String, category: String): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val date = dateFormat.format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Ensure the directory exists
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }

        Log.d("Saved to local directory", "path: ${storageDir}/${date}_${category}_${name}.jpg")

        return File(storageDir, "${date}_${category}_${name}.jpg")
    }

    private fun openGallery() {
        val pickPhotoIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    private fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${uri.lastPathSegment}")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // Handle the camera image
                    pictureUri?.let { uri ->
                        // Update the UI with the captured image
                        // For example, you can set the image to an ImageView
                        val imageView = findViewById<ImageView>(R.id.image_view)
                        imageView.setImageURI(uri)
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    // Handle the gallery image
                    data?.data?.let { uri ->
                        pictureUri = uri
                        // Update the UI with the selected image
                        val imageView = findViewById<ImageView>(R.id.image_view)
                        imageView.setImageURI(uri)
                    }
                }
            }
        }
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

    private fun updateMealSnack() {
        val name = nameInput.text.toString()
        val caloriesText = caloriesInput.text.toString()
        selectedCategory = when (toggleGroup.checkedButtonId) {
            R.id.meals -> "Meal"
            R.id.snacks -> "Snack"
            else -> ""
        }

        // Validate input fields
        if (name.isEmpty() || caloriesText.isEmpty() || selectedTime.isEmpty() || selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val calories = caloriesText.toInt()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            progressDialog.show() // Show loading screen
            if (pictureUri != null) {
                uploadImageToFirebase(pictureUri!!, { imageUrl ->
                    mealSnack = mealSnack.copy(name = name, calories = calories, time = selectedTime, category = selectedCategory, pictureUrl = imageUrl)
                    updateFirestoreDocument()
                }, { exception ->
                    progressDialog.dismiss() // Dismiss loading screen on failure
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                })
            } else {
                mealSnack = mealSnack.copy(name = name, calories = calories, time = selectedTime, category = selectedCategory)
                updateFirestoreDocument()
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFirestoreDocument() {
        val mealSnackCollection = firestore.collection("meals_snacks")
        Log.d("UpdateMealSnackActivity", "Updating mealSnack with id: ${mealSnack.id}")
        mealSnackCollection.document(mealSnack.id).set(mealSnack)
            .addOnSuccessListener {
                progressDialog.dismiss() // Dismiss loading screen on success
                Toast.makeText(this, "Meal/Snack updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss() // Dismiss loading screen on failure
                Toast.makeText(this, "Failed to update meal/snack: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("UpdateMealSnackActivity", "Error updating mealSnack", e)
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}