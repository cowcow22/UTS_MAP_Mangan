package com.example.uts_map_mangan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class DiaryEntry(val name: String, val calories: String)

class DiaryAdapter(private val diaryList: List<DiaryEntry>) :
    RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diaryItem = diaryList[position]
        holder.tvDiaryItemName.text = diaryItem.name
        holder.tvDiaryItemCalories.text = diaryItem.calories
        // You can also load an image into imgDiaryItem if needed
    }

    override fun getItemCount() = diaryList.size

    class DiaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDiaryItem: ImageView = view.findViewById(R.id.imgDiaryItem)
        val tvDiaryItemName: TextView = view.findViewById(R.id.tvDiaryItemName)
        val tvDiaryItemCalories: TextView = view.findViewById(R.id.tvDiaryItemCalories)
    }
}
