package com.example.uts_map_mangan

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.api.services.drive.model.File as DriveFile

class ProfileFragment : Fragment() {

    companion object {
        private const val PROFILE_SETTINGS_REQUEST_CODE = 1001
    }

    private val REQUEST_AUTHORIZATION = 101

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
    private lateinit var removeAdsButton: Button
    private lateinit var sync_btn: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var driveService: Drive

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
        removeAdsButton = view.findViewById(R.id.remove_ads_button)
        sync_btn = view.findViewById(R.id.sync_btn)

        // Initialize driveService
        val credential = GoogleAccountCredential.usingOAuth2(
            requireContext(), listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = GoogleSignIn.getLastSignedInAccount(requireContext())?.account
        driveService = Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("YourAppName").build()

        // Retrieve user data from SharedPreferences
        val cachedName = sharedPreferences.getString("name", null)
        val cachedProfileUrl = sharedPreferences.getString("profileUrl", null)

        if (cachedName != null) {
            profileName.text = cachedName
        }

        if (cachedProfileUrl != null) {
            Glide.with(this).load(cachedProfileUrl).into(profileImage)
        }

        // Add this inside the onViewCreated method
        sync_btn.setOnClickListener {
            syncDiariesToGoogleDrive()
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
                        }

                        if (!profileUrl.isNullOrEmpty()) {
                            Glide.with(this).load(profileUrl).into(profileImage)
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
            val intent = Intent(activity, HistoryActivity::class.java)
            startActivityForResult(intent, PROFILE_SETTINGS_REQUEST_CODE)
        }

        // Handle feedback settings button click
        feedbackButton.setOnClickListener {
            val intent = Intent(activity, FeedbackSettingsActivity::class.java)
            startActivityForResult(intent, PROFILE_SETTINGS_REQUEST_CODE)
        }

        // Handle feedback settings button click
        removeAdsButton.setOnClickListener {
            val intent = Intent(activity, RemoveAdsActivity::class.java)
            startActivityForResult(intent, PROFILE_SETTINGS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Refresh the data
            loadUserData()
        } else if (requestCode == REQUEST_AUTHORIZATION) {
            if (resultCode == Activity.RESULT_OK) {
                syncDiariesToGoogleDrive()
            } else {
                Toast.makeText(requireContext(), "Authorization failed", Toast.LENGTH_SHORT).show()
            }
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
                        }

                        if (!profileUrl.isNullOrEmpty()) {
                            Glide.with(this).load(profileUrl).into(profileImage)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun syncDiariesToGoogleDrive() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Syncing diaries to Google Drive...")
            setCancelable(false)
            show()
        }

        firestore.collection("meals_snacks")
            .whereEqualTo("accountId", user.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    progressDialog.dismiss()
                    Toast.makeText(context, "No diaries to sync", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val totalEntries = documents.size()
                var processedEntries = 0

                for (document in documents) {
                    val pictureUrl = document.getString("pictureUrl")
                    val name = document.getString("name")
                    val category = document.getString("category")

                    if (pictureUrl != null && name != null && category != null) {
                        checkAndUploadDiary(pictureUrl, name, category, progressDialog, document) {
                            processedEntries++
                            if (processedEntries == totalEntries) {
                                progressDialog.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    "All diary entries synced successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        processedEntries++
                        if (processedEntries == totalEntries) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                requireContext(),
                                "All diary entries synced successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    context,
                    "Failed to retrieve diaries: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkAndUploadDiary(
        imageUrl: String,
        name: String,
        category: String,
        progressDialog: ProgressDialog,
        document: DocumentSnapshot,
        onComplete: () -> Unit
    ) {
        val user = firebaseAuth.currentUser ?: return

        val timestamp = document.getDate("timestamp") ?: Date()
        val hour = document.getString("time")

        val inputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val hourParsed = inputFormat.parse(hour)
        val hourFormatted = outputFormat.format(hourParsed)

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = dateFormat.format(timestamp)

        val query = "name='${date}_${hourFormatted}_${category}_${name}.jpg' and trashed=false"
        CoroutineScope(Dispatchers.IO).launch {
            val result: FileList =
                driveService.files().list().setQ(query).setSpaces("drive").execute()
            val fileExists = result.files.isNotEmpty()

            withContext(Dispatchers.Main) {
                if (fileExists) {
                    onComplete()
                } else {
                    downloadImageFromFirebase(
                        imageUrl,
                        name,
                        category,
                        progressDialog,
                        document,
                        onComplete
                    )
                }
            }
        }
    }

    private fun downloadImageFromFirebase(
        imageUrl: String,
        name: String,
        category: String,
        progressDialog: ProgressDialog,
        document: DocumentSnapshot,
        onComplete: () -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        val localFile = File.createTempFile("images", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            val uri = Uri.fromFile(localFile)
            saveImageToDrive(uri, name, category, progressDialog, document, onComplete)
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(context, "Failed to download image from Firebase", Toast.LENGTH_SHORT)
                .show()
            onComplete()
        }
    }

    private fun saveImageToDrive(
        uri: Uri,
        name: String,
        category: String,
        progressDialog: ProgressDialog,
        document: DocumentSnapshot,
        onComplete: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val folderId = getOrCreateFolder("ManganDiaries")
                val filePath = uri.path ?: return@launch
                val file = File(filePath)

                if (!file.exists()) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@ProfileFragment.requireContext(),
                            "File not found: ${file.absolutePath}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onComplete()
                    }
                    return@launch
                }

                val timestamp = document.getDate("timestamp") ?: Date()
                val hour = document.getString("time")

                val inputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                val hourParsed = inputFormat.parse(hour)
                val hourFormatted = outputFormat.format(hourParsed)

                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val date = dateFormat.format(timestamp)
                val fileName = "${date}_${hourFormatted}_${category}_${name}.jpg"

                val fileContent = FileContent("image/jpeg", file)
                val driveFile = DriveFile().apply {
                    this.name = fileName
                    parents = listOf(folderId)
                }

                driveService.files().create(driveFile, fileContent).execute()

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Failed to save image to Google Drive: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onComplete()
                }
            }
        }
    }

    private fun getOrCreateFolder(folderName: String): String {
        val query =
            "mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false"
        val result: FileList = driveService.files().list().setQ(query).setSpaces("drive").execute()
        val folder = result.files.firstOrNull()

        return if (folder == null) {
            val newFolder = DriveFile().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
            }
            val createdFolder = driveService.files().create(newFolder).setFields("id").execute()
            createdFolder.id
        } else {
            folder.id
        }
    }
}