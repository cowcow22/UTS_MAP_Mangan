package com.example.uts_map_mangan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class InputMealSnackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_meal_snack)
        findViewById<Button>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }
//    private fun addMealOrSnack(
//        editMealName: EditText,
//        editCalories: EditText,
//        editTime: EditText,
//        radioGroupMealType: RadioGroup
//    ) {
//        val mealName = editMealName.text.toString().trim()
//        val calories = editCalories.text.toString().toLongOrNull()
//        val time = editTime.text.toString().trim()
//        val isMeal = radioGroupMealType.checkedRadioButtonId == R.id.radioMeal
//
//        if (mealName.isEmpty() || calories == null || time.isEmpty()) {
//            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Your existing Firestore logic to add the meal/snack
//        val user = firebaseAuth.currentUser
//        user?.uid?.let { uid ->
//            val mealData = hashMapOf(
//                "mealName" to mealName,
//                "calories" to calories,
//                "time" to time,
//                "isMeal" to isMeal
//            )
//
//            firestore.collection("Users").document(uid).collection("MealsAndSnacks")
//                .add(mealData)
//                .addOnSuccessListener {
//                    Toast.makeText(context, "Meal/Snack added successfully", Toast.LENGTH_SHORT).show()
//                    editMealName.text.clear()
//                    editCalories.text.clear()
//                    editTime.text.clear()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(context, "Error adding meal/snack: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
}