package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.services.drive.DriveScopes

class WelcomePageGoogleDriveRequest : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_SIGN_IN = 100
        private const val TAG = "WelcomePageGoogleDriveRequest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_googledrive)

        val allowButton = findViewById<Button>(R.id.btn_allow)
        allowButton.setOnClickListener {
            signInToGoogleDrive()
        }

        val rejectButton = findViewById<Button>(R.id.btn_reject)
        rejectButton.setOnClickListener {
            // Navigate to BasePage if rejected
            val intent = Intent(this, BasePage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signInToGoogleDrive() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE),
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA)
            )
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Sign-in succeeded, navigate to the next activity
            val intent = Intent(this, BasePage::class.java)
            startActivity(intent)
            finish()
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            // Retry sign-in process
            signInToGoogleDrive()
        }
    }
}