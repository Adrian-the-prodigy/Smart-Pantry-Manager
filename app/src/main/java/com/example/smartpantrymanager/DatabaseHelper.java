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
        seedRecipes(db);
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


    private void seedRecipes(SQLiteDatabase db) {

        // Recipe 1
        seedRecipe(db, "Tomato Egg Scramble",
                "1. Chop the tomato.\n" +
                        "2. Beat the eggs and cook them in a pan.\n" +
                        "3. Add the tomato and stir until cooked.",
                new String[][]{
                        {"egg", "2", "item"},
                        {"tomato", "1", "item"},
                        {"salt", "1", "g"},
                        {"oil", "5", "ml"}
                });

        // Recipe 2
        seedRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n" +
                        "2. Pour into a lightly oiled pan.\n" +
                        "3. Add cheese, fold, and cook through.",
                new String[][]{
                        {"egg", "2", "item"},
                        {"cheese", "30", "g"},
                        {"oil", "5", "ml"},
                        {"salt", "1", "g"}
                });

        // Recipe 3
        seedRecipe(db, "Garlic Tomato Pasta",
                "1. Boil the pasta until tender.\n" +
                        "2. Cook garlic and tomato in oil.\n" +
                        "3. Mix the sauce with the pasta.",
                new String[][]{
                        {"pasta", "100", "g"},
                        {"tomato", "2", "item"},
                        {"garlic", "5", "g"},
                        {"oil", "10", "ml"}
                });

        // Recipe 4
        seedRecipe(db, "Banana Oatmeal",
                "1. Cook the oats with milk.\n" +
                        "2. Slice the banana.\n" +
                        "3. Serve the banana over the oatmeal.",
                new String[][]{
                        {"oats", "50", "g"},
                        {"milk", "200", "ml"},
                        {"banana", "1", "item"}
                });

        // Recipe 5
        seedRecipe(db, "Peanut Butter Toast",
                "1. Toast the bread.\n" +
                        "2. Spread peanut butter over the toast.",
                new String[][]{
                        {"bread", "2", "slice"},
                        {"peanut butter", "20", "g"}
                });

        // Recipe 6
        seedRecipe(db, "Cheese Toast",
                "1. Place cheese on the bread.\n" +
                        "2. Toast until the cheese melts.",
                new String[][]{
                        {"bread", "2", "slice"},
                        {"cheese", "40", "g"}
                });

        // Recipe 7
        seedRecipe(db, "Garlic Fried Rice",
                "1. Heat the oil in a pan.\n" +
                        "2. Add garlic and cooked rice.\n" +
                        "3. Stir-fry until heated through.",
                new String[][]{
                        {"rice", "150", "g"},
                        {"garlic", "5", "g"},
                        {"oil", "10", "ml"},
                        {"salt", "1", "g"}
                });

        // Recipe 8
        seedRecipe(db, "Simple Tomato Rice",
                "1. Cook the rice.\n" +
                        "2. Chop and cook the tomatoes in oil.\n" +
                        "3. Mix together and season with salt.",
                new String[][]{
                        {"rice", "100", "g"},
                        {"tomato", "2", "item"},
                        {"oil", "10", "ml"},
                        {"salt", "1", "g"}
                });

        // Recipe 9
        seedRecipe(db, "Banana Pancakes",
                "1. Mash the banana.\n" +
                        "2. Mix with egg, flour, and milk.\n" +
                        "3. Cook small pancakes in a lightly oiled pan.",
                new String[][]{
                        {"banana", "1", "item"},
                        {"egg", "1", "item"},
                        {"flour", "60", "g"},
                        {"milk", "60", "ml"},
                        {"oil", "5", "ml"}
                });

        // Recipe 10
        seedRecipe(db, "Basic French Toast",
                "1. Beat the egg with milk.\n" +
                        "2. Dip the bread into the mixture.\n" +
                        "3. Cook both sides in a lightly oiled pan.",
                new String[][]{
                        {"bread", "2", "slice"},
                        {"egg", "1", "item"},
                        {"milk", "50", "ml"},
                        {"oil", "5", "ml"}
                });

        // Recipe 11
        seedRecipe(db, "Garlic Cheese Pasta",
                "1. Boil the pasta.\n" +
                        "2. Cook garlic gently in oil.\n" +
                        "3. Toss with pasta and cheese.",
                new String[][]{
                        {"pasta", "100", "g"},
                        {"garlic", "5", "g"},
                        {"cheese", "30", "g"},
                        {"oil", "10", "ml"}
                });

        // Recipe 12
        seedRecipe(db, "Tomato Cheese Sandwich",
                "1. Slice the tomato.\n" +
                        "2. Put tomato and cheese between bread slices.\n" +
                        "3. Serve fresh or toast until the cheese melts.",
                new String[][]{
                        {"bread", "2", "slice"},
                        {"tomato", "1", "item"},
                        {"cheese", "30", "g"}
                });

        // Recipe 13
        seedRecipe(db, "Simple Garlic Potatoes",
                "1. Cut the potatoes into small pieces.\n" +
                        "2. Toss with oil, garlic, and salt.\n" +
                        "3. Roast until tender and golden.",
                new String[][]{
                        {"potato", "200", "g"},
                        {"garlic", "5", "g"},
                        {"oil", "10", "ml"},
                        {"salt", "1", "g"}
                });

        // Recipe 14
        seedRecipe(db, "Mashed Potatoes",
                "1. Boil the potatoes until soft.\n" +
                        "2. Drain and mash with milk and butter.\n" +
                        "3. Season with salt.",
                new String[][]{
                        {"potato", "250", "g"},
                        {"milk", "50", "ml"},
                        {"butter", "15", "g"},
                        {"salt", "1", "g"}
                });

        // Recipe 15
        seedRecipe(db, "Apple Oat Bowl",
                "1. Cook oats with milk.\n" +
                        "2. Chop the apple.\n" +
                        "3. Serve the apple with the oatmeal.",
                new String[][]{
                        {"oats", "50", "g"},
                        {"milk", "200", "ml"},
                        {"apple", "1", "item"}
                });

        // Recipe 16
        seedRecipe(db, "Buttered Garlic Bread",
                "1. Mix softened butter with crushed garlic.\n" +
                        "2. Spread over the bread.\n" +
                        "3. Toast until lightly golden.",
                new String[][]{
                        {"bread", "2", "slice"},
                        {"butter", "15", "g"},
                        {"garlic", "5", "g"}
                });

        // Recipe 17
        seedRecipe(db, "Creamy Cheese Rice",
                "1. Cook the rice.\n" +
                        "2. Stir in milk and cheese over low heat.\n" +
                        "3. Season with salt and serve.",
                new String[][]{
                        {"rice", "100", "g"},
                        {"milk", "50", "ml"},
                        {"cheese", "40", "g"},
                        {"salt", "1", "g"}
                });

        // Recipe 18
        seedRecipe(db, "Potato Egg Hash",
                "1. Dice and cook the potato until tender.\n" +
                        "2. Add oil to a pan and cook the potato.\n" +
                        "3. Add the egg and cook through.",
                new String[][]{
                        {"potato", "150", "g"},
                        {"egg", "2", "item"},
                        {"oil", "10", "ml"},
                        {"salt", "1", "g"}
                });

        // Recipe 19
        seedRecipe(db, "Apple Banana Smoothie",
                "1. Peel and chop the banana.\n" +
                        "2. Core and chop the apple.\n" +
                        "3. Blend both fruits with milk until smooth.",
                new String[][]{
                        {"banana", "1", "item"},
                        {"apple", "1", "item"},
                        {"milk", "200", "ml"}
                });

        // Recipe 20
        seedRecipe(db, "Tomato Garlic Toast",
                "1. Chop the tomato and garlic.\n" +
                        "2. Cook them briefly in oil.\n" +
                        "3. Spoon over toasted bread.",
                new String[][]{
                        {"bread", "2", "slice"},
                        {"tomato", "1", "item"},
                        {"garlic", "5", "g"},
                        {"oil", "5", "ml"}
                });

    }

    private void seedRecipe(SQLiteDatabase db,
                            String name,
                            String steps,
                            String[][] ingredients) {

        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_STEPS, steps);

        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        if (recipeId == -1) {
            return;
        }

        for (String[] ingredient : ingredients) {

            ContentValues ingredientValues = new ContentValues();

            ingredientValues.put(COL_RECIPE_FK, recipeId);
            ingredientValues.put(COL_INGREDIENT_NAME, ingredient[0]);
            ingredientValues.put(
                    COL_REQUIRED_QUANTITY,
                    Double.parseDouble(ingredient[1])
            );
            ingredientValues.put(COL_REQUIRED_UNIT, ingredient[2]);

            db.insert(
                    TABLE_RECIPE_INGREDIENTS,
                    null,
                    ingredientValues
            );
        }
    }

}

