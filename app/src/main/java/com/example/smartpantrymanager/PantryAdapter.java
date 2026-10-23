package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final Cursor cursor;
    private final Context context;

    public PantryAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public static class PantryViewHolder extends RecyclerView.ViewHolder {

        TextView textIngredientName;
        TextView textIngredientQuantity;
        TextView textIngredientExpiry;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            textIngredientName =
                    itemView.findViewById(R.id.textIngredientName);

            textIngredientQuantity =
                    itemView.findViewById(R.id.textIngredientQuantity);

            textIngredientExpiry =
                    itemView.findViewById(R.id.textIngredientExpiry);
        }
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder, int position) {

        if (cursor == null || !cursor.moveToPosition(position)) {
            return;
        }

        int id = cursor.getInt(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_ID));

        String name = cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));

        double quantity = cursor.getDouble(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_QUANTITY));

        String unit = cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT));

        String expiryDate = cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPIRY_DATE));

        holder.textIngredientName.setText(name);

        holder.textIngredientQuantity.setText(
                "Quantity: " + quantity + " " + unit);

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            holder.textIngredientExpiry.setText("Expiry: Not specified");
        } else {
            holder.textIngredientExpiry.setText("Expiry: " + expiryDate);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditIngredientActivity.class);
            intent.putExtra("INGREDIENT_ID", id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }
}
