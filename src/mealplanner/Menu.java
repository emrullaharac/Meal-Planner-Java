package mealplanner;

import mealplanner.enums.CATEGORIES;

import java.sql.SQLException;
import java.util.*;

public class Menu {

    private static final Scanner SCANNER = new Scanner(System.in);
    private final Set<String> categories = new LinkedHashSet<>();
    private final DBManager database;
    private final List<Meal> meals;
    private final Planner planner;


    public Menu() {
        this.meals = new ArrayList<>();
        try {
            database = new DBManager();
            meals.addAll(database.getMealsFromDB("TRUE"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.planner = new Planner(SCANNER, database);
        Arrays.stream(CATEGORIES.values()).map(Enum::toString).forEach(categories::add);
    }

    public void work() {

        while(true) {
            System.out.println("What would you like to do (add, show, plan, save, exit)?");
            String input = SCANNER.nextLine();

            if ("exit".equals(input)) {
                database.close();
                System.out.println("Bye!");
                break;
            }

            if ("add".equals(input)) {
                if (add()) {
                    System.out.println("The meal has been added!");
                }
            }

            if ("show".equals(input)) {
                System.out.println(show());
            }

            if ("plan".equals(input)) {
                planner.plan();
            }

            if ("save".equals(input)) {
                planner.save();
            }
        }
    }

    private boolean isValid(String input) {
        String regex = "[a-zA-Z]+[a-zA-Z\\s]*";
        return (input != null && input.matches(regex));
    }

    public boolean add() {
        int meal_id = meals.size() + 1;
        String category = getCategoryInput("Which meal do you want to add (breakfast, lunch, dinner)?");
        String mealName = getMealInput("Input the meal's name:");
        String[] ingredientsInput = getIngredientsInput("Input the ingredients:");

        List<String> ingredients = new ArrayList<>(Arrays.asList(ingredientsInput));
        Meal meal = new Meal(meal_id, category, mealName, ingredients);
        meals.add(meal);

        // add meals and ingredients to database
        try {
            database.addMealToDB(meal);
        } catch (SQLException ignored) {
            //System.out.println("Meal cannot be added");
        }

        return meals.contains(meal);
    }

    public String getMealInput(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }
        String input;
        while(!isValid(input = SCANNER.nextLine())) {
            System.out.println("Wrong format. Use letters only!");
        }
        return input;
    }

    public String getCategoryInput(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }
        boolean isFound = false;
        String input;

        do {
            input = SCANNER.nextLine();
            if (categories.contains(input)) {
                if (isValid(input)) {
                    isFound = true;
                } else {
                    System.out.println("Wrong format. Use letters only!");
                }
            } else {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }

        } while (!isFound);

        return input;
    }

    public String[] getIngredientsInput(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }
        String[] ingredients;

        boolean isFound = false;
        do {
            ingredients = SCANNER.nextLine().trim().split(", *");
            boolean inputCheck = true;

            for (String input : ingredients) {
                if (!isValid(input)) {
                    System.out.println("Wrong format. Use letters only!");
                    inputCheck = false;
                    break;
                }
            }
            if (inputCheck) {
                isFound = true;
            }
        } while (!isFound);

        return ingredients;
    }

    public String show() {
        String category = getCategoryInput("Which category do you want to print (breakfast, lunch, dinner)?");
        StringBuilder message = new StringBuilder();

        List<Meal> mealsByCategory = database.getMealsFromDB(String.format("category = '%s'", category));

        if (mealsByCategory.isEmpty()) {
            message.append("No meals found.");
        } else {
            message.append("Category: ").append(category).append("\n");
            mealsByCategory.forEach(message::append);
        }

        return message.toString();
    }
}

