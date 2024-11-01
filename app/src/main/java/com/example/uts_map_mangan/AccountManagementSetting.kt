package com.example.uts_map_mangan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AccountManagementSetting : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var email_text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_management_setting)

        firebaseAuth = FirebaseAuth.getInstance()

        email_text = findViewById(R.id.email_text)
        email_text.text = firebaseAuth.currentUser?.email

        val emailTextView: TextView = findViewById(R.id.email_text)
        val deleteButton: Button = findViewById(R.id.delete_button)

        // Set the email_text to the user's email
        val user = firebaseAuth.currentUser
        user?.let {
            emailTextView.text = it.email
        }

        // Set the delete button functionality
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(user)
        }

        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
    }

    private fun showDeleteConfirmationDialog(user: FirebaseUser?) {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteUserAccount(user)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteUserAccount(user: FirebaseUser?) {
        user?.let { user ->
            val googleSignInAccount: GoogleSignInAccount? =
                GoogleSignIn.getLastSignedInAccount(this)
            googleSignInAccount?.let {
                val credential = GoogleAuthProvider.getCredential(it.idToken, null)
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Delete user data from Firebase database or Firestore
                            val userId = user.uid
                            val db = FirebaseFirestore.getInstance()
                            db.collection("Users").document(userId)
                                .delete()
                                .addOnCompleteListener { deleteDataTask ->
                                    if (deleteDataTask.isSuccessful) {
                                        // Delete user account
                                        user.delete()
                                            .addOnCompleteListener { deleteTask ->
                                                if (deleteTask.isSuccessful) {
                                                    Toast.makeText(
                                                        this,
                                                        "Account deleted successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    // Clear SharedPreferences
                                                    val sharedPreferences = getSharedPreferences(
                                                        "UserProfile",
                                                        Context.MODE_PRIVATE
                                                    )
                                                    sharedPreferences.edit().clear().apply()

                                                    // Revoke authentication
                                                    firebaseAuth.signOut()
                                                    val googleSignInClient = GoogleSignIn.getClient(
                                                        this,
                                                        GoogleSignInOptions.Builder(
                                                            GoogleSignInOptions.DEFAULT_SIGN_IN
                                                        ).build()
                                                    )
                                                    googleSignInClient.revokeAccess()
                                                        .addOnCompleteListener {
                                                            // Redirect to MainActivity
                                                            val intent = Intent(
                                                                this,
                                                                MainActivity::class.java
                                                            )
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "Failed to delete account: ${deleteTask.exception?.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Failed to delete user data: ${deleteDataTask.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Re-authentication failed: ${reauthTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }
}