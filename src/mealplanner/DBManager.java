package mealplanner;

import mealplanner.enums.CATEGORIES;
import mealplanner.enums.DAYS;

import java.sql.*;
import java.util.*;

public class DBManager {

    private static final String SELECT_DATA = "SELECT %s FROM %s";
    private static final String DELETE_DATA = "DELETE FROM %s";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    private static final String INSERT_MEAL = "INSERT INTO %s VALUES ('%s', '%s', %d)";
    private static final String INSERT_INGREDIENT = "INSERT INTO ingredients VALUES ('%s', %d, %d)";

    private final Connection conn;

    public DBManager() throws SQLException{
        this.conn = establishConn();
        createTables();
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException ignored) {}
    }

    private Connection establishConn() throws SQLException{
        final String DB_URL = "jdbc:postgresql://localhost:5432/meals_db";
        final String USER = "postgres";
        final String PASS = "1111";

        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        conn.setAutoCommit(true);

        return conn;
    }

    private void dropTables(String... tables) throws SQLException{

        try {
            for (String table : tables) {
                String dropTable = String.format(DROP_TABLE, table);
                PreparedStatement query = conn.prepareStatement(dropTable);
                query.executeUpdate();
            }
        } catch (Exception ignored) {
            //System.out.println("dropTables() failed");
        }
    }

    private void createTables() throws SQLException {
        String mealsTable = "CREATE TABLE IF NOT EXISTS meals (" +
                "category VARCHAR(50), " +
                "meal VARCHAR(50), " +
                "meal_id INTEGER)";
        //String table1 = "CREATE TABLE IF NOT EXISTS meals (category VARCHAR(50), meal VARCHAR(50), meal_id INTEGER )";
        String ingredientsTable = "CREATE TABLE IF NOT EXISTS ingredients (" +
                "ingredient VARCHAR(50), " +
                "ingredient_id INTEGER, " +
                "meal_id INTEGER)";

        String planTable  = "CREATE TABLE IF NOT EXISTS plan (" +
                "meal VARCHAR(50), " +
                "category VARCHAR(50), " +
                "meal_id INTEGER)";

        PreparedStatement query;

        // creating meals table
        query = conn.prepareStatement(mealsTable);
        query.executeUpdate();

        // creating ingredients table
        query = conn.prepareStatement(ingredientsTable);
        query.executeUpdate();

        // creating plan table
        query = conn.prepareStatement(planTable);
        query.executeUpdate();
    }

    public void addMealToDB(Meal meal) throws SQLException{
        List<String> ingredients = meal.getIngredients();

        try {
            int ingredientSize = getMax("ingredients", "ingredient_id") + 1;
            int meal_id = meal.getMeal_id();
            String meal_name = meal.getName();
            String category = meal.getCategory();

            String mealDB = String.format(INSERT_MEAL, "meals", category, meal_name, meal_id);
            insertDB(mealDB);

            for (String ingredient : ingredients) {
                String ingredientDB = String.format(INSERT_INGREDIENT, ingredient, ingredientSize, meal_id);
                insertDB(ingredientDB);
                ingredientSize++;
            }
        } catch (SQLException ignored) {
            //System.out.println("addMealToDB() failed");
        }
    }

    public void addPlanToDB(Map<String, Map<String, Meal>> plan) {
        try {

            // delete the plan from DB
            String deletePlan = String.format(DELETE_DATA, "plan");
            insertDB(deletePlan);

            // add new plan
            for (String day : plan.keySet()) {
                Map<String, Meal> dailyPlan = plan.get(day);
                for (String category : dailyPlan.keySet()) {
                    Meal meal = dailyPlan.get(category);
                    String planDB = String.format(INSERT_MEAL, "plan", meal.getName(), category, meal.getMeal_id());
                    insertDB(planDB);
                }
            }
        } catch (SQLException ignored) {
            //System.out.println("addPlanToDB() failed");
        }

    }

    private void insertDB(String query) throws SQLException {
        PreparedStatement insert = conn.prepareStatement(query);
        insert.executeUpdate();
    }

    public int getMax(String table, String column) {
        String query = String.format("SELECT MAX(%s) as MAX FROM %s", column, table);
        try {
            ResultSet rs = conn.prepareStatement(query).executeQuery();
            if (rs.next()) {
                return rs.getInt("MAX");
            }
        } catch (SQLException ignored) {
            //System.out.println("getMax() did not work");
        }
        return 0;
    }

    public int aggregate(String column, String table, String condition) {
        String query = String.format("SELECT %s FROM %s WHERE %s", column, table, condition);
        try {
            ResultSet rs = conn.prepareStatement(query).executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
            //System.out.println("getMax() did not work");
        }
        return 0;
    }

    public Map<String, Map<String, Meal>> getPlanFromDB() {
        Map<String, Map<String, Meal>> weeklyPlan = new LinkedHashMap<>();
        String readPlan = String.format(SELECT_DATA, "*", "plan");
        List<Meal> mealsOnPlan = new ArrayList<>();

        try (ResultSet rs = getResults(readPlan)) {
            while (rs.next()) {
                int id = rs.getInt("meal_id");
                List<Meal> meal = getMealsFromDB(String.format("meals.meal_id = %d", id));
                mealsOnPlan.add(meal.get(0));
            }
        } catch (SQLException ignored) {
            //System.out.println("getPlanFromDB() failed");
        }

        List<CATEGORIES> categories = List.of(CATEGORIES.values());
        List<DAYS> days = List.of(DAYS.values());

        for (int i = 0; i < mealsOnPlan.size(); i++) {
            // The plan totally have 21 meals before we added plan with 7 days and 3 categories each day
            // so the day and the category can be calculated as we have 3 meals each day
            // when the index divided by 3 it gives the day for 3 times so category can be changed respectively.
            String day = days.get(i / 3).toString();
            String category = categories.get(i % 3).toString();
            Meal meal = mealsOnPlan.get(i);

            Map<String, Meal> dailyPlan = weeklyPlan.get(day);
            if (dailyPlan == null) {
                dailyPlan = new LinkedHashMap<>();
            }
            dailyPlan.put(category, meal);
            weeklyPlan.put(day, dailyPlan);
        }

        return weeklyPlan;
    }

    public List<Meal> getMealsFromDB(String condition) {
        String readMeals = "SELECT * FROM meals JOIN ingredients ON meals.meal_id = ingredients.meal_id WHERE %s";
        readMeals = String.format(readMeals, condition);
        Map<Integer, Meal> mealMap = new LinkedHashMap<>();

        try (ResultSet rs = getResults(readMeals)) {
            while (rs.next()) {
                int meal_id = rs.getInt("meal_id");
                String category = rs.getString("category");
                String name = rs.getString("meal");
                String ingredient = rs.getString("ingredient");

                if (mealMap.containsKey(meal_id)) {
                    Meal meal = mealMap.get(meal_id);
                    meal.getIngredients().add(ingredient);
                } else {
                    Meal newMeal = new Meal(meal_id, category, name, new ArrayList<>());
                    newMeal.getIngredients().add(ingredient);
                    mealMap.put(meal_id, newMeal);
                }
            }
        } catch (Exception ignored) {}
        return new ArrayList<>(mealMap.values());
    }

    private ResultSet getResults(String read) throws SQLException {

        PreparedStatement query = conn.prepareStatement(read);
        return query.executeQuery();
    }

}