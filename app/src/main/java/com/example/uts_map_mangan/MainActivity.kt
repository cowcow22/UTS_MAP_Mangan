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

class MainActivity : AppCompatActivity() {

    // Variable Declaration START
    private lateinit var googleSignInBtn: Button
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val RC_SIGN_IN = 20
    // Variable Declaration END

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        googleSignInBtn = findViewById(R.id.google_sign_in_btn)
        googleSignInBtn.setOnClickListener {
            val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val gsc: GoogleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = gsc.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Menangani klik tombol "Create a New Account"
        val btnCreateNewAccount = findViewById<Button>(R.id.btn_create_new_account)
        btnCreateNewAccount.setOnClickListener {
            // Membuat Intent untuk membuka WelcomePageNameActivity
            val intent = Intent(this, WelcomePageNameActivity::class.java)
            startActivity(intent)
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
                    val map = hashMapOf(
                        "id" to user?.uid,
                        "name" to user?.displayName,
                        "profile" to user?.photoUrl.toString()
                    )
                    database.reference.child("Users").child(user?.uid!!)
                        .updateChildren(map as Map<String, Any>)
                    // HomePage bisa dibuah ke page lain
                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Error: Something went wrong!", Toast.LENGTH_SHORT).show()
                }
            }
    }
    // GOOGLE SIGN IN END
}