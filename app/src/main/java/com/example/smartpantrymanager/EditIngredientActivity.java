package com.example.smartpantrymanager;


import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditIngredientActivity extends AppCompatActivity {

    private EditText editIngredientName;
    private EditText editIngredientQuantity;
    private EditText editIngredientExpiry;
    private Spinner spinnerIngredientUnit;

    private DatabaseHelper databaseHelper;
    private int ingredientId;

    private final String[] units = {
            "item", "slice", "g", "kg", "ml", "l"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ingredient);

        databaseHelper = new DatabaseHelper(this);

        ingredientId = getIntent().getIntExtra("INGREDIENT_ID", -1);

        editIngredientName = findViewById(R.id.editIngredientName);
        editIngredientQuantity = findViewById(R.id.editIngredientQuantity);
        editIngredientExpiry = findViewById(R.id.editIngredientExpiry);
        spinnerIngredientUnit = findViewById(R.id.spinnerIngredientUnit);

        Button buttonUpdate = findViewById(R.id.buttonUpdateIngredient);
        Button buttonDelete = findViewById(R.id.buttonDeleteIngredient);
        Button buttonCancel = findViewById(R.id.buttonCancelEdit);

        if (ingredientId <= 0) {
            Toast.makeText(this, "Invalid ingredient.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadIngredient();

        buttonUpdate.setOnClickListener(v -> updateIngredient());
        buttonDelete.setOnClickListener(v -> confirmDelete());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void loadIngredient() {
        try (Cursor cursor = databaseHelper.getPantryItemById(ingredientId)) {

            if (!cursor.moveToFirst()) {
                Toast.makeText(this, "Ingredient not found.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));

            double quantity = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY));

            String unit = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT));

            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPIRY_DATE));

            editIngredientName.setText(name);
            editIngredientQuantity.setText(
                    String.valueOf(quantity));

            editIngredientExpiry.setText(
                    expiryDate == null ? "" : expiryDate);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    units
            );

            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);

            spinnerIngredientUnit.setAdapter(adapter);

            int selectedPosition = 0;

            for (int i = 0; i < units.length; i++) {
                if (units[i].equalsIgnoreCase(unit)) {
                    selectedPosition = i;
                    break;
                }
            }

            spinnerIngredientUnit.setSelection(selectedPosition);
        }
    }

    private void updateIngredient() {
        String name = editIngredientName.getText().toString().trim();
        String quantityText =
                editIngredientQuantity.getText().toString().trim();
        String unit = spinnerIngredientUnit.getSelectedItem().toString();
        String expiryDate = editIngredientExpiry.getText().toString().trim();

        if (name.isEmpty()) {
            editIngredientName.setError("Enter an ingredient name");
            editIngredientName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            editIngredientQuantity.setError("Enter a quantity");
            editIngredientQuantity.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);

            if (Double.isNaN(quantity)
                    || Double.isInfinite(quantity)
                    || quantity <= 0) {
                editIngredientQuantity.setError(
                        "Quantity must be greater than zero");
                editIngredientQuantity.requestFocus();
                return;
            }

        } catch (NumberFormatException e) {
            editIngredientQuantity.setError("Enter a valid quantity");
            editIngredientQuantity.requestFocus();
            return;
        }

        if (!expiryDate.isEmpty() && !isValidDate(expiryDate)) {
            editIngredientExpiry.setError(
                    "Use a valid date in YYYY-MM-DD format");
            editIngredientExpiry.requestFocus();
            return;
        }

        int result = databaseHelper.updatePantryItem(
                ingredientId,
                name,
                quantity,
                unit,
                expiryDate.isEmpty() ? null : expiryDate
        );

        if (result > 0) {
            Toast.makeText(
                    this,
                    "Ingredient updated successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(
                    this,
                    "No changes saved. Check that the ingredient still exists.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete this ingredient?")
                .setPositiveButton("Delete", (dialog, which) -> deleteIngredient())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteIngredient() {
        int result = databaseHelper.deletePantryItem(ingredientId);

        if (result > 0) {
            Toast.makeText(
                    this,
                    "Ingredient deleted.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(
                    this,
                    "Could not delete the ingredient.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private boolean isValidDate(String dateString) {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        dateFormat.setLenient(false);

        try {
            Date date = dateFormat.parse(dateString);
            return date != null && dateFormat.format(date).equals(dateString);
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
