package com.example.uts_map_mangan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipesAdapter(private val recipes: List<RecipeEntry>) :
    RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivRecipeItemImage)
        val nameTextView: TextView = itemView.findViewById(R.id.tvRecipeItemName)
        val caloriesTextView: TextView = itemView.findViewById(R.id.tvRecipeItemCalories)
        val timeTextView: TextView = itemView.findViewById(R.id.tvRecipeItemTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.imageView.setImageResource(recipe.imageResId)
        holder.nameTextView.text = recipe.name
        holder.caloriesTextView.text = recipe.calories
        holder.timeTextView.text = recipe.time
    }

    override fun getItemCount(): Int {
        return recipes.size
    }
}