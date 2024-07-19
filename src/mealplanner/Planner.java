package mealplanner;

import mealplanner.enums.CATEGORIES;
import mealplanner.enums.DAYS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Planner {
    private final Scanner scanner;
    private final DBManager database;
    private Map<String, Map<String, Meal>> weeklyPlan;

    public Planner(Scanner scanner, DBManager database) {
        this.scanner = scanner;
        this.database = database;
        weeklyPlan = database.getPlanFromDB();
    }

    private Map<String, Integer> getShoppingList() {
        if (weeklyPlan.isEmpty()) {
            return null;
        }

        Map<String, Integer> shoppingList = new LinkedHashMap<>();
        for (String day : weeklyPlan.keySet()) {

            Map<String, Meal> dailyPlan = weeklyPlan.get(day);
            for (String category : dailyPlan.keySet()) {

                Meal meal = dailyPlan.get(category);
                for (String ingredient : meal.getIngredients()) {

                    if (!shoppingList.containsKey(ingredient)) {
                        shoppingList.put(ingredient, 1);
                    } else {
                        shoppingList.compute(ingredient, (k, count) -> (count + 1));
                    }
                }
            }
        }

        return shoppingList;
    }

    public void save() {
        Map<String, Integer> shoppingList = getShoppingList();

        if (shoppingList != null) {
            System.out.println("Input a filename:");
            String fileName = scanner.nextLine();
            File file = new File(fileName);

            try (PrintWriter writer = new PrintWriter(file)) {
                for (String ingredient : shoppingList.keySet()) {
                    int amount = shoppingList.get(ingredient);
                    writer.printf(ingredient + (amount > 1 ? " x" + amount : ""));
                    writer.println();
                }
                System.out.println("Saved!");
            } catch (FileNotFoundException ignored) {
                System.out.println("File Not Found " + fileName);
            }
        } else {
            System.out.println("Unable to save. Plan your meals first.");
        }
    }

    public void plan() {
        Map<String, Map<String, Meal>> newPlan = new LinkedHashMap<>();
        for (DAYS day : DAYS.values()) {
            System.out.println(day);
            Map<String, Meal> dailyPlan = new LinkedHashMap<>();
            // list of chosen meals for the category
            for (CATEGORIES category : CATEGORIES.values()) {
                String query = String.format("category = '%s' ORDER BY meal", category);
                // get the list of mealsByCategory from DB
                List<Meal> mealsByCategory = database.getMealsFromDB(query);
                // print meals and input message
                mealsByCategory.forEach(elem -> System.out.println(elem.getName()));
                System.out.printf("Choose the %s for %s from the list above:%n", category, day);
                // Get input from user and check if it's proper
                Meal meal = chooseMeal(mealsByCategory);
                //System.out.println(meal);
                dailyPlan.put(category.toString(), meal);
            }
            System.out.printf("Yeah! We planned the meals for %s.%n%n", day);
            // add chosen meal list to the day of the plan
            newPlan.put(day.toString(), dailyPlan);
        }
        // changing new plan with old one
        weeklyPlan = newPlan;
        // print the plan
        printPlan();
        // save plan to DB
        database.addPlanToDB(weeklyPlan);
    }

    private void printPlan() {
        for (String day : weeklyPlan.keySet()) {
            System.out.println(day);
            Map<String, Meal> dailyPlan = weeklyPlan.get(day);
            for (String category : dailyPlan.keySet()) {
                Meal meal = dailyPlan.get(category);
                System.out.printf("%s: %s%n", capitalize(category), meal.getName());
            }
            System.out.println();
        }
    }

    private String capitalize(String str)
    {
        if(str == null || str.length()<=1) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private Meal chooseMeal(List<Meal> mealList) {
        Map<String, Meal> meals = new HashMap<>();
        mealList.forEach(elem -> meals.put(elem.getName(), elem));

        while (true) {
            String input = scanner.nextLine();
            Meal found = meals.get(input);
            if (found != null) {
                return found;
            } else {
                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
            }
        }
    }
}
