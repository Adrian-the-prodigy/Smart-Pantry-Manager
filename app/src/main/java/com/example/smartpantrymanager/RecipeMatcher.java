package com.example.smartpantrymanager;


import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    private final DatabaseHelper databaseHelper;

    public RecipeMatcher(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Information about a recipe returned by the matcher.
    public static class SuggestedRecipe {
        private final int id;
        private final String name;
        private final String steps;

        public SuggestedRecipe(int id, String name, String steps) {
            this.id = id;
            this.name = name;
            this.steps = steps;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSteps() {
            return steps;
        }
    }

    // Converts quantities to a common base unit.
    private static class UnitInfo {
        final String dimension;
        final double factor;

        UnitInfo(String dimension, double factor) {
            this.dimension = dimension;
            this.factor = factor;
        }
    }

    private UnitInfo getUnitInfo(String unit) {
        if (unit == null) {
            return null;
        }

        String normalized = unit.trim()
                .toLowerCase(Locale.ROOT)
                .replace(".", "");

        switch (normalized) {
            case "kg":
            case "kilogram":
            case "kilograms":
                return new UnitInfo("weight", 1000.0);

            case "g":
            case "gram":
            case "grams":
                return new UnitInfo("weight", 1.0);

            case "l":
            case "litre":
            case "litres":
            case "liter":
            case "liters":
                return new UnitInfo("volume", 1000.0);

            case "ml":
            case "millilitre":
            case "millilitres":
            case "milliliter":
            case "milliliters":
                return new UnitInfo("volume", 1.0);

            case "item":
            case "items":
            case "piece":
            case "pieces":
            case "pc":
            case "pcs":
            case "each":
                return new UnitInfo("item", 1.0);

            case "slice":
            case "slices":
                return new UnitInfo("slice", 1.0);

            default:
                return null;
        }
    }

    // Normalizes common ingredient-name variations.
    private String normalizeIngredient(String name) {
        if (name == null) {
            return "";
        }

        String normalized = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        // Explicit common variations.
        switch (normalized) {
            case "tomatoes":
                return "tomato";

            case "potatoes":
                return "potato";

            case "eggs":
                return "egg";

            case "apples":
                return "apple";

            case "bananas":
                return "banana";

            case "onions":
                return "onion";

            case "carrots":
                return "carrot";

            case "loaves":
                return "loaf";

            default:
                break;
        }

        // Basic singularization for other regular plurals.
        if (normalized.length() > 4
                && normalized.endsWith("ies")) {
            return normalized.substring(
                    0, normalized.length() - 3) + "y";
        }

        if (normalized.length() > 4
                && normalized.endsWith("oes")) {
            return normalized.substring(
                    0, normalized.length() - 2);
        }

        if (normalized.length() > 3
                && normalized.endsWith("s")
                && !normalized.endsWith("ss")
                && !normalized.endsWith("us")) {
            return normalized.substring(
                    0, normalized.length() - 1);
        }

        return normalized;
    }

    // Creates a consistent key for an ingredient and unit type.
    private String createKey(String ingredient, String dimension) {
        return normalizeIngredient(ingredient) + "|" + dimension;
    }

    // Loads pantry quantities, converting compatible units.
    private Map<String, Double> loadPantryQuantities() {
        Map<String, Double> pantry = new HashMap<>();

        try (Cursor cursor = databaseHelper.getAllPantryItems()) {

            int nameIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_NAME);
            int quantityIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_QUANTITY);
            int unitIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_UNIT);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                double quantity = cursor.getDouble(quantityIndex);
                String unit = cursor.getString(unitIndex);

                UnitInfo unitInfo = getUnitInfo(unit);

                // Unknown units cannot safely be compared.
                if (unitInfo == null || quantity <= 0) {
                    continue;
                }

                String key = createKey(name, unitInfo.dimension);
                double baseQuantity = quantity * unitInfo.factor;

                pantry.put(
                        key,
                        pantry.getOrDefault(key, 0.0) + baseQuantity
                );
            }
        }

        return pantry;
    }

    // Returns only recipes for which every required ingredient
    // exists in sufficient quantity.
    public List<SuggestedRecipe> getSuggestedRecipes() {

        List<SuggestedRecipe> suggestions = new ArrayList<>();

        Map<String, Double> pantry = loadPantryQuantities();

        try (Cursor recipes = databaseHelper.getAllRecipes()) {

            int idIndex = recipes.getColumnIndexOrThrow(
                    DatabaseHelper.COL_RECIPE_ID);
            int nameIndex = recipes.getColumnIndexOrThrow(
                    DatabaseHelper.COL_RECIPE_NAME);
            int stepsIndex = recipes.getColumnIndexOrThrow(
                    DatabaseHelper.COL_RECIPE_STEPS);

            while (recipes.moveToNext()) {

                int recipeId = recipes.getInt(idIndex);
                String recipeName = recipes.getString(nameIndex);
                String recipeSteps = recipes.getString(stepsIndex);

                Map<String, Double> required =
                        loadRecipeRequirements(recipeId);

                // An empty or invalid recipe must not match.
                if (required.isEmpty()) {
                    continue;
                }

                boolean matches = true;

                for (Map.Entry<String, Double> entry
                        : required.entrySet()) {

                    String ingredientKey = entry.getKey();
                    double requiredQuantity = entry.getValue();

                    double availableQuantity =
                            pantry.getOrDefault(ingredientKey, 0.0);

                    if (availableQuantity + 0.000001
                            < requiredQuantity) {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    suggestions.add(
                            new SuggestedRecipe(
                                    recipeId,
                                    recipeName,
                                    recipeSteps
                            )
                    );
                }
            }
        }

        return suggestions;
    }

    // Loads and combines duplicate requirements for a recipe.
    private Map<String, Double> loadRecipeRequirements(int recipeId) {

        Map<String, Double> requirements = new HashMap<>();
        boolean invalidRequirement = false;

        try (Cursor cursor =
                     databaseHelper.getRecipeIngredients(recipeId)) {

            int nameIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_INGREDIENT_NAME);
            int quantityIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_REQUIRED_QUANTITY);
            int unitIndex = cursor.getColumnIndexOrThrow(
                    DatabaseHelper.COL_REQUIRED_UNIT);

            while (cursor.moveToNext()) {

                String name = cursor.getString(nameIndex);
                double quantity = cursor.getDouble(quantityIndex);
                String unit = cursor.getString(unitIndex);

                UnitInfo unitInfo = getUnitInfo(unit);

                if (unitInfo == null || quantity <= 0
                        || normalizeIngredient(name).isEmpty()) {
                    invalidRequirement = true;
                    break;
                }

                String key = createKey(name, unitInfo.dimension);
                double baseQuantity = quantity * unitInfo.factor;

                requirements.put(
                        key,
                        requirements.getOrDefault(key, 0.0)
                                + baseQuantity
                );
            }
        }

        // Never suggest a recipe whose requirements cannot
        // be interpreted safely.
        if (invalidRequirement) {
            requirements.clear();
        }

        return requirements;
    }
}

