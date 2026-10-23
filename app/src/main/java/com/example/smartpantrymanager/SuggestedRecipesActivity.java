package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerSuggestedRecipes;
    private TextView textEmptyRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        databaseHelper = new DatabaseHelper(this);

        recyclerSuggestedRecipes =
                findViewById(R.id.recyclerSuggestedRecipes);
        textEmptyRecipes = findViewById(R.id.textEmptyRecipes);

        Button buttonBackToPantry =
                findViewById(R.id.buttonBackToPantry);

        recyclerSuggestedRecipes.setLayoutManager(
                new LinearLayoutManager(this));

        buttonBackToPantry.setOnClickListener(v -> finish());

        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        RecipeMatcher recipeMatcher = new RecipeMatcher(databaseHelper);

        List<RecipeMatcher.SuggestedRecipe> recipes =
                recipeMatcher.getSuggestedRecipes();

        SuggestedRecipeAdapter adapter =
                new SuggestedRecipeAdapter(this, recipes);

        recyclerSuggestedRecipes.setAdapter(adapter);

        boolean isEmpty = recipes.isEmpty();

        textEmptyRecipes.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE);

        recyclerSuggestedRecipes.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
