package com.example.smartpantrymanager;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText editIngredientName;
    private EditText editIngredientQuantity;
    private EditText editIngredientExpiry;
    private Spinner spinnerIngredientUnit;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        databaseHelper = new DatabaseHelper(this);

        editIngredientName = findViewById(R.id.editIngredientName);
        editIngredientQuantity = findViewById(R.id.editIngredientQuantity);
        editIngredientExpiry = findViewById(R.id.editIngredientExpiry);
        spinnerIngredientUnit = findViewById(R.id.spinnerIngredientUnit);

        Button buttonSave = findViewById(R.id.buttonSaveIngredient);
        Button buttonCancel = findViewById(R.id.buttonCancelIngredient);

        buttonSave.setOnClickListener(v -> saveIngredient());

        buttonCancel.setOnClickListener(v -> finish());
    }

    private void saveIngredient() {

        String name = editIngredientName.getText().toString().trim();
        String quantityText =
                editIngredientQuantity.getText().toString().trim();
        String unit = spinnerIngredientUnit.getSelectedItem().toString();
        String expiryDate =
                editIngredientExpiry.getText().toString().trim();

        // Validate the ingredient name.
        if (name.isEmpty()) {
            editIngredientName.setError("Enter an ingredient name");
            editIngredientName.requestFocus();
            return;
        }

        // Validate the quantity.
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

        // Validate the optional expiry date.
        if (!expiryDate.isEmpty() && !isValidDate(expiryDate)) {
            editIngredientExpiry.setError(
                    "Use a valid date in YYYY-MM-DD format");
            editIngredientExpiry.requestFocus();
            return;
        }

        // Save the ingredient in SQLite.
        long result = databaseHelper.addPantryItem(
                name, quantity, unit,
                expiryDate.isEmpty() ? null : expiryDate
        );

        if (result != -1) {
            Toast.makeText(
                    this,
                    "Ingredient saved successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } else {
            Toast.makeText(
                    this,
                    "Could not save the ingredient.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private boolean isValidDate(String dateString) {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        dateFormat.setLenient(false);

        try {
            Date date = dateFormat.parse(dateString);

            return date != null
                    && dateFormat.format(date).equals(dateString);

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

