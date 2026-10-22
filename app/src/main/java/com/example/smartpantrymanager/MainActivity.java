package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerPantry;
    private TextView textEmptyPantry;
    private PantryAdapter pantryAdapter;
    private Cursor pantryCursor;

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);
        Button buttonAddIngredient = findViewById(R.id.buttonAddIngredient);

        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));

        buttonAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );

            startActivity(intent);
        });


        Button buttonSuggestedRecipes =
                findViewById(R.id.buttonSuggestedRecipes);

        buttonSuggestedRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SuggestedRecipesActivity.class);

            startActivity(intent);
        });


        Button buttonSettings = findViewById(R.id.buttonSettings);

        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );

            startActivity(intent);
        });
    }

    private void loadPantryItems() {
        if (pantryCursor != null && !pantryCursor.isClosed()) {
            pantryCursor.close();
        }

        pantryCursor = databaseHelper.getAllPantryItems();

        pantryAdapter = new PantryAdapter(this, pantryCursor);
        recyclerPantry.setAdapter(pantryAdapter);

        boolean isEmpty = pantryCursor == null || pantryCursor.getCount() == 0;

        textEmptyPantry.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerPantry.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (pantryCursor != null && !pantryCursor.isClosed()) {
            pantryCursor.close();
        }

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
