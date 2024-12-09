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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.Drive
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.DriveScopes
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.UUID
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InputMealSnackActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_AUTHORIZATION = 101
    private var pictureUri: Uri? = null
    private var selectedTime: String? = null
    private lateinit var driveService: Drive
    private lateinit var credential: GoogleAccountCredential
    private var isUploading = false // Add this flag
    private var mealSnack: MealSnack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_meal_snack)

        mealSnack = intent.getParcelableExtra("mealSnack")

        mealSnack?.let {
            // Populate the fields with existing data
            findViewById<EditText>(R.id.foodname_input).setText(it.name)
            findViewById<EditText>(R.id.calories_input).setText(it.calories.toString())
            findViewById<Button>(R.id.add_button).text = "Update"
        }

        findViewById<Button>(R.id.add_button).setOnClickListener {
            saveOrUpdateMealSnack()
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE),
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA)
            )
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            setupDriveService(account)
        } else {
            startActivityForResult(googleSignInClient.signInIntent, 1000)
        }

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
            checkGoogleDriveAccess()
        }
    }


    private fun saveOrUpdateMealSnack() {
        val name = findViewById<EditText>(R.id.foodname_input).text.toString()
        val calories = findViewById<EditText>(R.id.calories_input).text.toString().toInt()

        // Add new meal/snack logic
        val newMealSnack = MealSnack(
            id = UUID.randomUUID().toString(), // Generate a unique ID
            name = name,
            calories = calories,
            timestamp = Date()
        )
        saveMealSnackToDatabase(newMealSnack)

        finish()
    }

    private fun saveMealSnackToDatabase(mealSnack: MealSnack) {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val mealSnackCollection = firestore.collection("meals_snacks")
            mealSnackCollection.document(mealSnack.id).set(mealSnack)
                .addOnSuccessListener {
                    Toast.makeText(this, "Meal/Snack added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed to add meal/snack: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupDriveService(account: GoogleSignInAccount) {
        credential = GoogleAccountCredential.usingOAuth2(this, listOf(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = account.account
        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Map_Mangan").build()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == Activity.RESULT_OK) {
            // Retry the operation after user grants permission
            addMealOrSnack()
        } else if (requestCode == REQUEST_AUTHORIZATION && resultCode != Activity.RESULT_OK) {
            // If the user denies permission, finish the activity
            Toast.makeText(
                this,
                "Permission denied. Cannot save image to Google Drive.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        if (requestCode == 1000) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    setupDriveService(account)
                }
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        }
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

    private fun checkGoogleDriveAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE),
                    com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA)
                )
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            startActivityForResult(googleSignInClient.signInIntent, 1000)
        } else {
            val saveOnlyDrive = true
            addMealOrSnack(saveOnlyDrive)
        }
    }

    private fun addMealOrSnack(saveOnlyDrive: Boolean = false) {
        if (isUploading) return // Prevent multiple uploads
        isUploading = true

        val name = findViewById<EditText>(R.id.foodname_input).text.toString()
        val calories = findViewById<EditText>(R.id.calories_input).text.toString().toIntOrNull()
        val time = selectedTime ?: ""
        val category = if (findViewById<MaterialButton>(R.id.meals).isChecked) "Meal" else "Snack"

        if (name.isEmpty()) {
            Toast.makeText(this, "Name must be filled", Toast.LENGTH_SHORT).show()
            isUploading = false
            return
        }

        if (calories == null) {
            Toast.makeText(this, "Calories must be filled", Toast.LENGTH_SHORT).show()
            isUploading = false
            return
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "Time must be selected", Toast.LENGTH_SHORT).show()
            isUploading = false
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Category must be selected", Toast.LENGTH_SHORT).show()
            isUploading = false
            return
        }

        if (pictureUri == null) {
            Toast.makeText(this, "Picture must be added", Toast.LENGTH_SHORT).show()
            isUploading = false
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            isUploading = false
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
                val firestore = FirebaseFirestore.getInstance()
                val mealSnackCollection = firestore.collection("meals_snacks")
                val newMealSnack = MealSnack(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    calories = calories,
                    time = time,
                    category = category,
                    pictureUrl = uri.toString(),
                    timestamp = Date(), // Set the current date and time
                    accountId = currentUser.uid
                )
                mealSnackCollection.add(newMealSnack)
                    .addOnSuccessListener { documentReference ->
                        val documentId = documentReference.id
                        mealSnackCollection.document(documentId).update("id", documentId)
                            .addOnSuccessListener {
                                if (saveOnlyDrive) {
                                    // Download the image from Firebase Storage
                                    downloadImageFromFirebase(
                                        newMealSnack.id,
                                        uri.toString(),
                                        name,
                                        category,
                                        progressDialog
                                    )
                                } else {
                                    downloadImageFromFirebase(
                                        newMealSnack.id,
                                        uri.toString(),
                                        name,
                                        category,
                                        progressDialog
                                    )
                                }
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Failed to update meal/snack ID: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isUploading = false
                            }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to add diary entry", Toast.LENGTH_SHORT).show()
                        isUploading = false
                    }
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            isUploading = false
        }
    }

    private fun downloadImageFromFirebase(
        id: String,
        imageUrl: String,
        name: String,
        category: String,
        progressDialog: ProgressDialog
    ) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        val localFile = File.createTempFile("images", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            // Image downloaded successfully, now upload it to Google Drive
            val uri = Uri.fromFile(localFile)
            saveImageToDrive(id, uri, name, category, progressDialog)
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to download image from Firebase", Toast.LENGTH_SHORT)
                .show()
            isUploading = false
        }
    }

    private fun saveImageToDrive(
        id: String,
        uri: Uri,
        name: String,
        category: String,
        progressDialog: ProgressDialog
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filePath = uri.path ?: return@launch
                val file = File(filePath)

                // Ensure the file exists
                if (!file.exists()) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@InputMealSnackActivity,
                            "File not found: ${file.absolutePath}",
                            Toast.LENGTH_SHORT
                        ).show()
                        isUploading = false
                    }
                    return@launch
                }

                // Generate the file name using the current date and time, category, and name
                val timestamp = Date()
                val hour = selectedTime ?: "00:00 AM"

                val inputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                val hourParsed = inputFormat.parse(hour)
                val hourFormatted = outputFormat.format(hourParsed)

                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val date = dateFormat.format(timestamp)
                val fileName = "${id}_${date}_${hourFormatted}_${category}_${name}.jpg"

                val fileContent = FileContent("image/jpeg", file)
                val driveFile = DriveFile().apply {
                    this.name = fileName
                    parents = listOf("appDataFolder")
                }

                driveService.files().create(driveFile, fileContent).execute()

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@InputMealSnackActivity,
                        "File saved to Google Drive successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                    isUploading = false
                }
            } catch (e: UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
                    isUploading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@InputMealSnackActivity,
                        "Failed to save image to Google Drive: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isUploading = false
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}