package com.example.uts_map_mangan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private val diaryList = mutableListOf<DiaryEntryClass>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_settings)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerViewMeal)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        diaryAdapter = DiaryAdapter(diaryList, isLoading = false) // Assuming isLoading is a boolean
        recyclerView.adapter = diaryAdapter

        findViewById<Button>(R.id.back_button_notification).setOnClickListener {
            onBackPressed()
        }

        fetchDiaries()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    private fun fetchDiaries() {
        val user = firebaseAuth.currentUser
        user?.uid?.let { uid ->
            firestore.collection("meals_snacks")
                .whereEqualTo("accountId", uid)
                .get()
                .addOnSuccessListener { result ->
                    diaryList.clear()
                    for (document in result) {
                        val diary = document.toObject(DiaryEntryClass::class.java)
                        diaryList.add(diary)
                    }
                    diaryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }
}