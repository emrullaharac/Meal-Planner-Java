package mealplanner;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    private final int meal_id;
    private final String category;
    private final String name;
    private final List<String> ingredients;

    public Meal(int meal_id, String category, String name, List<String> ingredients) {
        this.meal_id = meal_id;
        this.category = category;
        this.name = name;
        this.ingredients = new ArrayList<>(ingredients);
    }

    public int getMeal_id() {
        return meal_id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getIngredientsResult() {
        StringBuilder res = new StringBuilder();

        for (String ingredient : ingredients) {
            res.append("\n").append(ingredient);
        }

        return res.toString();
    }

    public List<String> getIngredients() {
        return ingredients;
    }



    @Override
    public String toString() {
        return  "\nName: " + getName() +
                "\nIngredients: " + getIngredientsResult()
                + "\n";
    }
}
