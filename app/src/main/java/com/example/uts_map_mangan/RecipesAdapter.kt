// RecipesAdapter.kt
package com.example.uts_map_mangan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipesAdapter(
    private val recipesList: List<RecipeEntry>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(recipe: RecipeEntry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipesList[position]
        holder.bind(recipe, itemClickListener)
    }

    override fun getItemCount(): Int = recipesList.size

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeImage: ImageView = itemView.findViewById(R.id.ivRecipeItemImage)
        private val recipeName: TextView = itemView.findViewById(R.id.tvRecipeItemName)
        private val recipeCalories: TextView = itemView.findViewById(R.id.tvRecipeItemCalories)
        private val recipeTime: TextView = itemView.findViewById(R.id.tvRecipeItemTime)

        fun bind(recipe: RecipeEntry, clickListener: OnItemClickListener) {
            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(recipe.imageResId) // Placeholder image
                .error(R.drawable.food_image_example) // Error image
                .into(recipeImage)
            recipeName.text = recipe.name
            recipeCalories.text = recipe.calories
            recipeTime.text = recipe.time

            itemView.setOnClickListener {
                clickListener.onItemClick(recipe)
            }
        }
    }
}