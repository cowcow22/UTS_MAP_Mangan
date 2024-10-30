package com.example.uts_map_mangan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class DiaryAdapter(private var diaryList: List<DiaryEntryClass>, private var isLoading: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_SKELETON = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SKELETON else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SKELETON) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diary_skeleton, parent, false)
            SkeletonViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
            DiaryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DiaryViewHolder) {
            val diary = diaryList[position]
            holder.bind(diary)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else diaryList.size
    }

    fun updateList(newList: List<DiaryEntryClass>) {
        diaryList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DiaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDiaryItem: ImageView = view.findViewById(R.id.imgDiaryItem)
        val tvDiaryItemName: TextView = view.findViewById(R.id.tvDiaryItemName)
        val tvDiaryItemCalories: TextView = view.findViewById(R.id.tvDiaryItemCalories)
        val tvDiaryItemTime: TextView = view.findViewById(R.id.tvDiaryItemTime)

        fun bind(diary: DiaryEntryClass) {
            Glide.with(imgDiaryItem.context)
                .load(diary.pictureUrl)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                .into(imgDiaryItem)
            tvDiaryItemName.text = diary.name
            tvDiaryItemCalories.text = diary.calories.toString()
            tvDiaryItemTime.text = diary.time
        }
    }

    class SkeletonViewHolder(view: View) : RecyclerView.ViewHolder(view)
}