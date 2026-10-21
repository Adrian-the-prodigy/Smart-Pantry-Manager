package com.example.smartpantrymanager;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY_DATE = "expiry_date";

    // Recipes table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // Recipe ingredients table
    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";
    public static final String COL_RECIPE_INGREDIENT_ID = "id";
    public static final String COL_RECIPE_FK = "recipe_id";
    public static final String COL_INGREDIENT_NAME = "ingredient_name";
    public static final String COL_REQUIRED_QUANTITY = "required_quantity";
    public static final String COL_REQUIRED_UNIT = "required_unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createPantryTable =
                "CREATE TABLE " + TABLE_PANTRY + " (" +
                        COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT NOT NULL, " +
                        COL_QUANTITY + " REAL NOT NULL, " +
                        COL_UNIT + " TEXT NOT NULL, " +
                        COL_EXPIRY_DATE + " TEXT" +
                        ")";

        String createRecipesTable =
                "CREATE TABLE " + TABLE_RECIPES + " (" +
                        COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_RECIPE_NAME + " TEXT NOT NULL, " +
                        COL_RECIPE_STEPS + " TEXT NOT NULL" +
                        ")";

        String createRecipeIngredientsTable =
                "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                        COL_RECIPE_INGREDIENT_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_RECIPE_FK + " INTEGER NOT NULL, " +
                        COL_INGREDIENT_NAME + " TEXT NOT NULL, " +
                        COL_REQUIRED_QUANTITY + " REAL NOT NULL, " +
                        COL_REQUIRED_UNIT + " TEXT NOT NULL, " +
                        "FOREIGN KEY (" + COL_RECIPE_FK + ") REFERENCES " +
                        TABLE_RECIPES + "(" + COL_RECIPE_ID + ") " +
                        "ON DELETE CASCADE" +
                        ")";

        db.execSQL(createPantryTable);
        db.execSQL(createRecipesTable);
        db.execSQL(createRecipeIngredientsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // Add a pantry ingredient
    public long addPantryItem(String name, double quantity,
                              String unit, String expiryDate) {

        if (name == null || name.trim().isEmpty()
                || quantity <= 0
                || unit == null || unit.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit.trim());
        values.put(COL_EXPIRY_DATE,
                expiryDate == null || expiryDate.trim().isEmpty()
                        ? null : expiryDate.trim());

        return db.insert(TABLE_PANTRY, null, values);
    }

    // Read all pantry ingredients
    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_PANTRY,
                null,
                null,
                null,
                null,
                null,
                COL_NAME + " COLLATE NOCASE ASC"
        );
    }

    // Read one pantry ingredient
    public Cursor getPantryItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_PANTRY,
                null,
                COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
    }

    // Update a pantry ingredient
    public int updatePantryItem(int id, String name,
                                double quantity, String unit,
                                String expiryDate) {

        if (id <= 0 || name == null || name.trim().isEmpty()
                || quantity <= 0
                || unit == null || unit.trim().isEmpty()) {
            return 0;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit.trim());
        values.put(COL_EXPIRY_DATE,
                expiryDate == null || expiryDate.trim().isEmpty()
                        ? null : expiryDate.trim());

        return db.update(
                TABLE_PANTRY,
                values,
                COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // Delete a pantry ingredient
    public int deletePantryItem(int id) {
        if (id <= 0) {
            return 0;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                TABLE_PANTRY,
                COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // Add a recipe
    public long addRecipe(String name, String steps) {

        if (name == null || name.trim().isEmpty()
                || steps == null || steps.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_RECIPE_NAME, name.trim());
        values.put(COL_RECIPE_STEPS, steps.trim());

        return db.insert(TABLE_RECIPES, null, values);
    }

    // Add one required ingredient to a recipe
    public long addRecipeIngredient(int recipeId, String ingredientName,
                                    double requiredQuantity,
                                    String requiredUnit) {

        if (recipeId <= 0
                || ingredientName == null
                || ingredientName.trim().isEmpty()
                || requiredQuantity <= 0
                || requiredUnit == null
                || requiredUnit.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_RECIPE_FK, recipeId);
        values.put(COL_INGREDIENT_NAME, ingredientName.trim());
        values.put(COL_REQUIRED_QUANTITY, requiredQuantity);
        values.put(COL_REQUIRED_UNIT, requiredUnit.trim());

        return db.insert(TABLE_RECIPE_INGREDIENTS, null, values);
    }

    // Read all recipes
    public Cursor getAllRecipes() {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                COL_RECIPE_NAME + " COLLATE NOCASE ASC"
        );
    }

    // Read the ingredients required by one recipe
    public Cursor getRecipeIngredients(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_RECIPE_INGREDIENTS,
                null,
                COL_RECIPE_FK + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                COL_INGREDIENT_NAME + " COLLATE NOCASE ASC"
        );
    }

    // Read one recipe's details
    public Cursor getRecipeById(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_RECIPES,
                null,
                COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null
        );
    }

    // Enable foreign key enforcement for database connections
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}

