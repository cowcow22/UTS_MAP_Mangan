package com.example.uts_map_mangan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class WelcomePageConfirmLoginActivity : AppCompatActivity() {

    private lateinit var googleSignInBtn: Button
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val RC_SIGN_IN = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_page_confirmlogin)

        googleSignInBtn = findViewById(R.id.google_sign_in_btn)
        googleSignInBtn.setOnClickListener {
            val gso: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            val gsc: GoogleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = gsc.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error: " + e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val firestore = FirebaseFirestore.getInstance()
                    val userDocRef = firestore.collection("Users").document(user?.uid!!)

                    userDocRef.get().addOnSuccessListener { document ->
                        if (!document.exists() || document.getString("name") == null || document.getString(
                                "profile"
                            ) == null
                        ) {
                            // User document does not exist or is missing information, proceed to WelcomePageNameActivity
                            val userMap = hashMapOf(
                                "id" to user.uid,
                                "name" to user.displayName,
                                "profile" to user.photoUrl.toString()
                            )
                            userDocRef.set(userMap).addOnSuccessListener {
                                val intent = Intent(this, WelcomePageNameActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            // User document exists and has necessary information, proceed to HomePage
                            val intent = Intent(this, BasePage::class.java)
                            startActivity(intent)
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error: Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}