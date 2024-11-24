package com.example.uts_map_mangan

import android.content.Intent
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

class DiaryAdapter(private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mealSnackList: List<MealSnack> = listOf()
    private var isLoading: Boolean = false

    interface OnItemClickListener {
        fun onItemClick(mealSnack: MealSnack)
    }

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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
            DiaryViewHolder(view, itemClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DiaryViewHolder) {
            val mealSnack = mealSnackList[position]
            holder.bind(mealSnack)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else mealSnackList.size
    }

    fun updateList(newList: List<MealSnack>) {
        mealSnackList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DiaryViewHolder(view: View, private val itemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(view) {
        private val imgDiaryItem: ImageView = view.findViewById(R.id.imgDiaryItem)
        private val tvDiaryItemName: TextView = view.findViewById(R.id.tvDiaryItemName)
        private val tvDiaryItemCalories: TextView = view.findViewById(R.id.tvDiaryItemCalories)
        private val tvDiaryItemTime: TextView = view.findViewById(R.id.tvDiaryItemTime)

        fun bind(mealSnack: MealSnack) {
            Glide.with(imgDiaryItem.context)
                .load(mealSnack.pictureUrl)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                .into(imgDiaryItem)
            tvDiaryItemName.text = mealSnack.name
            tvDiaryItemCalories.text = "${mealSnack.calories} cal"
            tvDiaryItemTime.text = mealSnack.time

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, InputMealSnackActivity::class.java)
                intent.putExtra("mealSnack", mealSnack)
                itemView.context.startActivity(intent)
            }
        }
    }

    class SkeletonViewHolder(view: View) : RecyclerView.ViewHolder(view)
}