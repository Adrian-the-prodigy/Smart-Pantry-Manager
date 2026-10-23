package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestedRecipeAdapter
        extends RecyclerView.Adapter<SuggestedRecipeAdapter.RecipeViewHolder> {

    private final Context context;
    private final List<RecipeMatcher.SuggestedRecipe> recipes;

    public SuggestedRecipeAdapter(
            Context context,
            List<RecipeMatcher.SuggestedRecipe> recipes) {
        this.context = context;
        this.recipes = recipes;
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView textRecipeName;
        TextView textRecipeSteps;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textRecipeSteps = itemView.findViewById(R.id.textRecipeSteps);
        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggested_recipe, parent, false);

        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecipeViewHolder holder, int position) {

        RecipeMatcher.SuggestedRecipe recipe = recipes.get(position);

        holder.textRecipeName.setText(recipe.getName());
        holder.textRecipeSteps.setText("Tap to view recipe details");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }
}

