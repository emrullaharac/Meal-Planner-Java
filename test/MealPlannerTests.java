import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;

import java.io.File;
import java.sql.*;
import java.util.*;

class Column {
  public String first;
  public String second;

  public Column(String first, String second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Column column = (Column) o;
    return Objects.equals(first, column.first) && Objects.equals(second, column.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }
}

class dbTable {
  String name;
  List<Column> columnNameType;

  public dbTable(String name, List<Column> columnNameType) {
    this.name = name;
    this.columnNameType = columnNameType;
  }
}

class MyMealTestData {
  String mealCategory;
  String mealName;
  String[] ingredients;

  MyMealTestData(String mealCategory, String mealName, String[] ingredients) {
    this.mealCategory = mealCategory;
    this.mealName = mealName;
    this.ingredients = ingredients;
  }
}

public class MealPlannerTests extends StageTest {

  static final String DB_URL = "jdbc:postgresql:meals_db";
  static final String USER = "postgres";
  static final String PASS = "1111";

  String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

  static final MyMealTestData[] mealsList = new MyMealTestData[]{
          new MyMealTestData("breakfast", "scrambled eggs", new String[]{"eggs", "milk", "cheese"}),
          new MyMealTestData("breakfast", "sandwich", new String[]{"bread", "cheese", "ham"}),
          new MyMealTestData("breakfast", "oatmeal", new String[]{"oats", "milk", "banana", "peanut butter"}),
          new MyMealTestData("breakfast", "english breakfast", new String[]{"eggs", "sausages", "bacon", "tomatoes",
                  "bread"}),
          new MyMealTestData("lunch", "sushi", new String[]{"salmon", "rice", "avocado"}),
          new MyMealTestData("lunch", "chicken salad", new String[]{"chicken", "lettuce", "tomato", "olives"}),
          new MyMealTestData("lunch", "omelette", new String[]{"eggs", "milk", "cheese"}),
          new MyMealTestData("lunch", "salad", new String[]{"lettuce", "tomato", "onion", "cheese", "olives"}),
          new MyMealTestData("dinner", "pumpkin soup", new String[]{"pumpkin", "coconut milk", "curry", "carrots"}),
          new MyMealTestData("dinner", "beef steak", new String[]{"beef steak"}),
          new MyMealTestData("dinner", "pizza", new String[]{"flour", "tomato", "cheese", "salami"}),
          new MyMealTestData("dinner", "tomato soup", new String[]{"tomato", "orzo"})
  };

  void checkTableSchema(List<dbTable> tables) {
    try {
      Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
      DatabaseMetaData meta = connection.getMetaData();
      for (dbTable table : tables) {
        ResultSet tableMeta = meta.getTables(null, null, table.name, null);
        if (!tableMeta.next() || !table.name.equalsIgnoreCase(tableMeta.getString("TABLE_NAME"))) {
          throw new WrongAnswer("The table \"" + table.name + "\" doesn't exist.");
        }
        ResultSet columns = meta.getColumns(null, null, table.name, null);
        List<Column> columnsData = new ArrayList<>();
        while (columns.next()) {
          Column column = new Column(
                  columns.getString("COLUMN_NAME").toLowerCase(),
                  columns.getString("TYPE_NAME").toLowerCase());
          columnsData.add(column);
        }
        for (Column c : table.columnNameType) {
          if (!columnsData.contains(c)) {
            for (Column c2 : columnsData) {
              if (c.first.equals(c2.first)) {
                throw new WrongAnswer("The column \"" + c.first + "\" of the table \"" + table.name + "\" is of the " +
                        "wrong type.");
              }
            }
            throw new WrongAnswer("The column \"" + c.first + "\" of the table \"" + table.name + "\" doesn't exist.");
          }
        }
      }
      connection.close();
    } catch (Exception e) {
      throw new WrongAnswer("An exception was thrown, while trying to check the database schema - " + e.getMessage());
    }
  }

  void checkConnection() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new WrongAnswer("An exception was thrown, while trying to connect to database. PostgreSQL JDBC Driver is " +
              "not found.");
    }
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DB_URL, USER, PASS);
    } catch (SQLException e) {
      throw new WrongAnswer("An exception was thrown, while trying to connect to database. Connection Failed");
    }

    if (connection == null) {
      throw new WrongAnswer("Failed to make connection to database");
    }
  }

  @DynamicTest(order = 0)
  public CheckResult normalExe21Test() {

    checkConnection();
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DB_URL, USER, PASS);
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to connect to database. Connection Failed");
    }
    try {
      Statement statement = connection.createStatement();
      statement.executeUpdate("DROP TABLE if exists plan");
      statement.executeUpdate("DROP TABLE if exists ingredients");
      statement.executeUpdate("DROP TABLE if exists meals");
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to drop tables - " + e);
    }

    CheckOutput co = new CheckOutput();
    if (!co.start("What would you like to do (add, show, plan, save, exit)?"))
      return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, save," +
              " exit)?\"");
    ArrayList<dbTable> tables = new ArrayList<>(Arrays.asList(
            new dbTable("ingredients", new ArrayList<>(
                    Arrays.asList(
                            new Column("ingredient", "varchar"),
                            new Column("ingredient_id", "int4"),
                            new Column("meal_id", "int4")
                    )
            )),
            new dbTable("meals", new ArrayList<>(
                    Arrays.asList(
                            new Column("category", "varchar"),
                            new Column("meal", "varchar"),
                            new Column("meal_id", "int4")
                    )
            ))
    ));
    checkTableSchema(tables);

    if (!co.input("save", "Unable to save. Plan your meals first."))
      return CheckResult.wrong("Your output should contain \"Unable to save. Plan your meals first.\"");

    if (!co.input("exit", "Bye!"))
      return CheckResult.wrong("Your output should contain \"Bye!\"");
    if (!co.programIsFinished())
      return CheckResult.wrong("The application didn't exit.");
    return CheckResult.correct();
  }

  @DynamicTest(order = 1)
  public CheckResult normalExe16Test() {

    checkConnection();
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DB_URL, USER, PASS);
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to connect to database. Connection Failed");
    }
    try {
      Statement statement = connection.createStatement();
      statement.executeUpdate("DROP TABLE if exists plan");
      statement.executeUpdate("DROP TABLE if exists ingredients");
      statement.executeUpdate("DROP TABLE if exists meals");
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to drop tables - " + e);
    }

    CheckOutput co = new CheckOutput();
    if (!co.start("What would you like to do (add, show, plan, save, exit)?"))
      return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, save," +
              " exit)?\"");
    ArrayList<dbTable> tables = new ArrayList<>(Arrays.asList(
            new dbTable("ingredients", new ArrayList<>(
                    Arrays.asList(
                            new Column("ingredient", "varchar"),
                            new Column("ingredient_id", "int4"),
                            new Column("meal_id", "int4")
                    )
            )),
            new dbTable("meals", new ArrayList<>(
                    Arrays.asList(
                            new Column("category", "varchar"),
                            new Column("meal", "varchar"),
                            new Column("meal_id", "int4")
                    )
            ))
    ));
    checkTableSchema(tables);

    if (!co.input("exit", "Bye!"))
      return CheckResult.wrong("Your output should contain \"Bye!\"");
    if (!co.programIsFinished())
      return CheckResult.wrong("The application didn't exit.");
    return CheckResult.correct();
  }

  @DynamicTest(order = 2)
  CheckResult normalExe17Test() {
    checkConnection();
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DB_URL, USER, PASS);
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to connect to database. Connection Failed");
    }
    try {
      Statement statement = connection.createStatement();
      statement.executeUpdate("DROP TABLE if exists plan");
      statement.executeUpdate("DROP TABLE if exists ingredients");
      statement.executeUpdate("DROP TABLE if exists meals");
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to drop tables - " + e);
    }

    try {
      CheckOutput co = new CheckOutput();
      if (!co.start("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("add", "Which meal do you want to add (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about meal category: \"(breakfast, lunch, dinner)" +
                "?\"");

      if (!co.input("lunch", "Input the meal's name:"))
        return CheckResult.wrong("Your output should contain \"Input the meal's name:\"");

      if (!co.input("sushi", "Input the ingredients:"))
        return CheckResult.wrong("Your output should contain \"Input the ingredients:\"");

      if (!co.input("salmon, rice, avocado", "The meal has been added!"))
        return CheckResult.wrong("Your output should contain \"The meal has been added!\"");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("add", "Which meal do you want to add (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about meal category: \"(breakfast, lunch, dinner)" +
                "?\"");

      if (!co.input("lunch", "Input the meal's name:"))
        return CheckResult.wrong("Your output should contain \"Input the meal's name:\"");

      if (!co.input("omelette", "Input the ingredients:"))
        return CheckResult.wrong("Your output should contain \"Input the ingredients:\"");

      if (!co.input("eggs, milk, cheese", "The meal has been added!"))
        return CheckResult.wrong("Your output should contain \"The meal has been added!\"");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("add", "Which meal do you want to add (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about meal category: \"(breakfast, lunch, dinner)" +
                "?\"");

      if (!co.input("breakfast", "Input the meal's name:"))
        return CheckResult.wrong("Your output should contain \"Input the meal's name:\"");

      if (!co.input("oatmeal", "Input the ingredients:"))
        return CheckResult.wrong("Your output should contain \"Input the ingredients:\"");

      if (!co.input("oats, milk, banana, peanut butter", "The meal has been added!"))
        return CheckResult.wrong("Your output should contain \"The meal has been added!\"");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("show", "Which category do you want to print (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about the meal category to print: \"(breakfast, " +
                "lunch, dinner)?\"");

      if (!co.input("lunch", "Category: lunch", "Name: sushi", "Ingredients:", "salmon", "rice", "avocado",
              "Name: omelette", "Ingredients:", "eggs", "milk", "cheese"))
        return CheckResult.wrong("Wrong \"show\" output for a saved meal.");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("show", "Which category do you want to print (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about the meal category to print: \"(breakfast, " +
                "lunch, dinner)?\"");

      if (!co.input("breakfast", "Category: breakfast", "Name: oatmeal", "Ingredients:", "oats",
              "milk", "banana", "peanut butter"))
        return CheckResult.wrong("Wrong \"show\" output for a saved meal.");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("exit", "Bye!"))
        return CheckResult.wrong("Your output should contain \"Bye!\"");

      if (!co.programIsFinished())
        return CheckResult.wrong("The application didn't exit.");
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown while testing - " + e);
    }

    return CheckResult.correct();
  }

  @DynamicTest(order = 3)
  CheckResult normalExe18Test() {
    checkConnection();
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DB_URL, USER, PASS);
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to connect to database. Connection Failed");
    }

    try {
      CheckOutput co = new CheckOutput();
      if (!co.start("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("show", "Which category do you want to print (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about the meal category to print: \"(breakfast, " +
                "lunch, dinner)?\"");

      if (!co.input("lunch", "Category: lunch", "Name: sushi", "Ingredients:", "salmon", "rice", "avocado",
              "Name: omelette", "Ingredients:", "eggs", "milk", "cheese"))
        return CheckResult.wrong("Wrong \"show\" output for a saved meal.");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("show", "Which category do you want to print (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about the meal category to print: \"(breakfast, " +
                "lunch, dinner)?\"");

      if (!co.input("breakfast", "Category: breakfast", "Name: oatmeal", "Ingredients:", "oats",
              "milk", "banana", "peanut butter"))
        return CheckResult.wrong("Wrong \"show\" output for a saved meal.");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");

      if (!co.input("show", "Which category do you want to print (breakfast, lunch, dinner)?"))
        return CheckResult.wrong("Your program should ask the user about the meal category to print: \"(breakfast, " +
                "lunch, dinner)?\"");

      if (!co.input("brunch", "Wrong meal category! Choose from: breakfast, lunch, dinner."))
        return CheckResult.wrong("Wrong output after the input of a category that doesn't exist.");

      if (!co.input("dinner", "No meals found."))
        return CheckResult.wrong("Wrong output for a category with no added meals.");

      if (!co.input("exit", "Bye!"))
        return CheckResult.wrong("Your output should contain \"Bye!\"");

      if (!co.programIsFinished())
        return CheckResult.wrong("The application didn't exit.");
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown while testing - " + e);
    }

    return CheckResult.correct();
  }

  @DynamicTest(order = 4)
  CheckResult normalExe19Test() {
    checkConnection();
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DB_URL, USER, PASS);
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to connect to database. Connection Failed");
    }
    try {
      Statement statement = connection.createStatement();
      statement.executeUpdate("DROP TABLE if exists ingredients");
      statement.executeUpdate("DROP TABLE if exists plan");
      statement.executeUpdate("DROP TABLE if exists meals");
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to drop tables - " + e);
    }

    try {
      CheckOutput co = new CheckOutput();
      if (!co.start("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                "save, " +
                "exit)?\"");


      for (MyMealTestData meal : mealsList) {
        if (!co.input("add", "Which meal do you want to add (breakfast, lunch, dinner)?"))
          return CheckResult.wrong("Your program should ask the user about meal category: \"(breakfast, lunch, " +
                  "dinner)?\"");

        if (!co.input(meal.mealCategory, "Input the meal's name:"))
          return CheckResult.wrong("Your output should contain \"Input the meal's name:\"");

        if (!co.input(meal.mealName, "Input the ingredients:"))
          return CheckResult.wrong("Your output should contain \"Input the ingredients:\"");

        if (!co.input(String.join(",", meal.ingredients), "The meal has been added!"))
          return CheckResult.wrong("Your output should contain \"The meal has been added!\"");

        if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
          return CheckResult.wrong("Your program should ask the user about the required action: \"(add, show, plan, " +
                  "save, " +
                  "exit)?\"");

      }

      co.getNextOutput("plan");
      int index = 0;
      for (String day : days) {
        if (!co.inputNext(day))
          return CheckResult.wrong("Your output should contain \"" + day + "\"");

        String[] categories = new String[]{"breakfast", "lunch", "dinner"};
        String[][] alphabetic = {
                new String[]{"english breakfast", "oatmeal", "sandwich", "scrambled eggs"},
                new String[]{"chicken salad", "omelette", "salad", "sushi"},
                new String[]{"beef steak", "pizza", "pumpkin soup", "tomato soup"}
        };
        for (int i = 0; i < 3; i++) {
          String category = categories[i];
          if (!co.inputNext(alphabetic[i]))
            return CheckResult.wrong("Make sure that formatting of your output is similar to the one in the example. " +
                    "Also, your output should contain the meals in alphabetic order.");

          if (!co.inputNext("Choose the " + category + " for " + day + " from the list above:"))
            return CheckResult.wrong("Your output should contain the prompt for the " + category + " meal.");

          if (!co.input("nonExistMeal", "This meal doesn’t exist. Choose a meal from the list above."))
            return CheckResult.wrong("Your output should contain \"This meal doesn’t exist. Choose a meal from the " +
                    "list above.\"");

          co.getNextOutput(((MyMealTestData) (Arrays.stream(mealsList).filter(x -> x.mealCategory.equals(category)).toArray()[index % 4])).mealName);
        }
        if (!co.inputNext("Yeah! We planned the meals for " + day + "."))
          return CheckResult.wrong("Your output should contain \"Yeah! We planned the meals for " + day + ".\".");
        index++;
      }

      String[] planPrintout = new String[]{"Monday", "Breakfast: scrambled eggs", "Lunch: sushi", "Dinner: pumpkin " +
              "soup",
              "Tuesday", "Breakfast: sandwich", "Lunch: chicken salad", "Dinner: beef steak",
              "Wednesday", "Breakfast: oatmeal", "Lunch: omelette", "Dinner: pizza",
              "Thursday", "Breakfast: english breakfast", "Lunch: salad", "Dinner: tomato soup",
              "Friday", "Breakfast: scrambled eggs", "Lunch: sushi", "Dinner: pumpkin soup",
              "Saturday", "Breakfast: sandwich", "Lunch: chicken salad", "Dinner: beef steak",
              "Sunday", "Breakfast: oatmeal", "Lunch: omelette", "Dinner: pizza"};

      for (String line : planPrintout) {
        if (!co.inputNext(line))
          return CheckResult.wrong("Your output should contain \"" + line + "\".");
      }

      if (!co.input("exit", "Bye!"))
        return CheckResult.wrong("Your output should contain \"Bye!\"");

      if (!co.programIsFinished())
        return CheckResult.wrong("The application didn't exit.");

    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown while testing - " + e);
    }

    return CheckResult.correct();
  }

  @DynamicTest(order = 5)
  CheckResult normalExe20Test() {
    checkConnection();
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DB_URL, USER, PASS);
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown, while trying to connect to database. Connection Failed");
    }

    try {
      CheckOutput co = new CheckOutput();
      if (!co.start("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong(
                "Your program should ask the user about the required action: \"(add, show, plan, save, exit)?\"");

      if (!co.input("save", "Input a filename:"))
        return CheckResult.wrong("Your output should contain: \"Input a filename:\"");

      if (!co.input("plan.txt", "Saved!"))
        return CheckResult.wrong("Your output should contain: \"Saved!\"");

      if (!co.inputNext("What would you like to do (add, show, plan, save, exit)?"))
        return CheckResult.wrong(
                "Your program should ask the user about the required action: \"(add, show, plan, save, exit)?\"");

      File ingredientsFile = new File("plan.txt");
      if (!ingredientsFile.exists())
        return CheckResult.wrong("The ingredient file doesn't exist.");

      List<String> ingredientsInPlan = new ArrayList<>(List.of(
              "avocado x2", "bacon", "banana x2", "beef steak x2", "bread x3", "carrots x2",
              "cheese x9", "chicken x2", "coconut milk x2", "curry x2", "eggs x5",
              "flour x2", "ham x2", "lettuce x3", "milk x6", "oats x2", "olives x3",
              "onion", "orzo", "peanut butter x2", "pumpkin x2", "rice x2", "salami x2",
              "salmon x2", "sausages", "tomato x6", "tomatoes"));
      Scanner scanner = new Scanner(ingredientsFile);
      while(scanner.hasNext()){
        String line = scanner.nextLine();
        if (!ingredientsInPlan.contains(line))
          return CheckResult.wrong("There is no ingredient \""+line+"\" in plan");
        ingredientsInPlan.remove(line);
      }
      if(!ingredientsInPlan.isEmpty())
        return CheckResult.wrong("Ingredient \""+ingredientsInPlan.get(0)+"\" doesn't exist in file.");

      if (!co.input("exit", "Bye!"))
        return CheckResult.wrong("Your output should contain \"Bye!\"");

      if (!co.programIsFinished())
        return CheckResult.wrong("The application didn't exit.");
    } catch (Exception e) {
      return CheckResult.wrong("An exception was thrown while testing - "+e);
    }

    return CheckResult.correct();
  }
}


class CheckOutput {
  private TestedProgram main = new TestedProgram();
  private int position = 0;
  private boolean caseInsensitive = true;
  private boolean trimOutput = true;
  private List<String> arguments = new ArrayList<>();
  private boolean isStarted = false;
  private String lastOutput = "";

  private boolean checkOutput(String outputString, String... checkStr) {
    int searchPosition = position;
    for (String cStr : checkStr) {
      String str = caseInsensitive ? cStr.toLowerCase() : cStr;
      int findPosition = outputString.indexOf(str, searchPosition);
      if (findPosition == -1) return false;
      if (!outputString.substring(searchPosition, findPosition).isBlank()) return false;
      searchPosition = findPosition + str.length();
    }
    position = searchPosition;
    return true;
  }

  public boolean start(String... checkStr) {
    if (isStarted)
      return false;
    var outputString = main.start(arguments.toArray(new String[]{}));
    lastOutput = outputString;
    if (trimOutput) outputString = outputString.trim();
    if (caseInsensitive) outputString = outputString.toLowerCase();
    isStarted = true;
    return checkOutput(outputString, checkStr);
  }

  public void stop() {
    main.stop();
  }

  public boolean input(String input, String... checkStr) {
    if (main.isFinished()) return false;
    String outputString = main.execute(input);
    lastOutput = outputString;
    if (trimOutput) outputString = outputString.trim();
    if (caseInsensitive) outputString = outputString.toLowerCase();
    position = 0;
    return checkOutput(outputString, checkStr);
  }

  public boolean inputNext(String... checkStr) {
    String outputString = lastOutput;
    if (trimOutput) outputString = outputString.trim();
    if (caseInsensitive) outputString = outputString.toLowerCase();
    return checkOutput(outputString, checkStr);
  }

  public String getNextOutput(String input) {
    if (main.isFinished()) return "";
    String outputString = main.execute(input);
    lastOutput = outputString;
    position = 0;
    return outputString;
  }

  public String getLastOutput() {
    return lastOutput;
  }

  public boolean programIsFinished() {
    return main.isFinished();
  }

  public void setArguments(String... arguments) {
    this.arguments = Arrays.stream(arguments).toList();
  }

  public void setCaseInsensitive(boolean caseInsensitive) {
    this.caseInsensitive = caseInsensitive;
  }

  public void setTrimOutput(boolean trimOutput) {
    this.trimOutput = trimOutput;
  }
}