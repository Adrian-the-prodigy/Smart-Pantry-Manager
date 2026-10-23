package com.example.smartpantrymanager;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private TextView textRecipeDetailName;
    private TextView textRecipeDetailIngredients;
    private TextView textRecipeDetailSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        databaseHelper = new DatabaseHelper(this);

        textRecipeDetailName =
                findViewById(R.id.textRecipeDetailName);

        textRecipeDetailIngredients =
                findViewById(R.id.textRecipeDetailIngredients);

        textRecipeDetailSteps =
                findViewById(R.id.textRecipeDetailSteps);

        Button buttonBackToRecipes =
                findViewById(R.id.buttonBackToRecipes);

        buttonBackToRecipes.setOnClickListener(v -> finish());

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);

        if (recipeId == -1) {
            Toast.makeText(
                    this,
                    "Recipe could not be opened.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadRecipeDetails(recipeId);
    }

    private void loadRecipeDetails(int recipeId) {

        try (Cursor recipeCursor =
                     databaseHelper.getRecipeById(recipeId)) {

            if (recipeCursor == null || !recipeCursor.moveToFirst()) {
                Toast.makeText(
                        this,
                        "Recipe not found.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
                return;
            }

            String recipeName = recipeCursor.getString(
                    recipeCursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_RECIPE_NAME));

            String steps = recipeCursor.getString(
                    recipeCursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_RECIPE_STEPS));

            textRecipeDetailName.setText(recipeName);
            textRecipeDetailSteps.setText(steps);
        }

        StringBuilder ingredientsText = new StringBuilder();

        try (Cursor ingredientsCursor =
                     databaseHelper.getRecipeIngredients(recipeId)) {

            if (ingredientsCursor != null &&
                    ingredientsCursor.moveToFirst()) {

                do {
                    String name = ingredientsCursor.getString(
                            ingredientsCursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_INGREDIENT_NAME));

                    double quantity = ingredientsCursor.getDouble(
                            ingredientsCursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_REQUIRED_QUANTITY));

                    String unit = ingredientsCursor.getString(
                            ingredientsCursor.getColumnIndexOrThrow(
                                    DatabaseHelper.COL_REQUIRED_UNIT));

                    ingredientsText
                            .append("• ")
                            .append(name)
                            .append(" — ")
                            .append(quantity)
                            .append(" ")
                            .append(unit)
                            .append("\n");

                } while (ingredientsCursor.moveToNext());
            }
        }

        if (ingredientsText.length() == 0) {
            ingredientsText.append("No ingredients listed.");
        }

        textRecipeDetailIngredients.setText(ingredientsText.toString());
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
