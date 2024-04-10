import java.util.*;
import java.sql.*;


public class FoodTracker {
    private static final String DB_URL = "jdbc:postgresql://ep-ancient-king-a18jamhc.ap-southeast-1.aws.neon.tech/FoodTracker?user=Hariharan1828&password=nB1iV6hKbOql&sslmode=require";
    private static final String DB_USER = "Hariharan1828";
    private static final String DB_PASSWORD = "nB1iV6hKbOql";

    public static List<Map<String, Object>> Users = new ArrayList<>();
    public static List<Map<String, Object>> Inventory = new ArrayList<>();
    public static boolean isLoggedIn = false;
    public static String currentUser = "";

    public static void main(String[] args) throws SQLException {
        FoodItems.FoodTracker();
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("Database connection established.");

        DatabaseHelper dbHelper = new DatabaseHelper(conn);


        Scanner sc = new Scanner(System.in);
        boolean exitRequested = false;

        while (!exitRequested) {
            if (!isLoggedIn) {
                System.out.println("\n=============================");
                System.out.println("Welcome to Food Tracker");
                System.out.println("=============================");
                System.out.println("Enter your Choice: ");
                System.out.println("1. Register Yourself");
                System.out.println("2. Login");
                System.out.println("3. Add Motivational Diet Quote");
                System.out.println("4. View Motivational Diet Quotes");
                System.out.print("Choice: ");
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        Users.addAll(Registration.register_details());
                        System.out.println("Registration Successful!");
                        break;
                    case 2:
                        System.out.print("Enter your name: ");
                        String name = sc.nextLine();
                        if (login(name)) {
                            System.out.println("Login Successful!");
                            isLoggedIn = true;
                            currentUser = name;
                        } else {
                            System.out.println("Login failed. User not found.");
                        }
                        break;
                    case 3:
                        System.out.print("Enter the motivational diet quote: ");
                        String quote = sc.nextLine();
                        dbHelper.addMotivationalQuote(quote);
                        break;
                    case 4:
                        System.out.println("Motivational Diet Quotes:");
                        List<String> quotes = dbHelper.getAllMotivationalQuotes();
                        for (String q : quotes) {
                            System.out.println(q);
                        }
                        break;
                    case 5:
                        exitRequested = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else {
                displayUserMenu(sc);
            }
        }
        System.out.println("Program ended.");
    }

    public static boolean login(String name) {
        for (Map<String, Object> user : Users) {
            if (user.get("Name").toString().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    public static void displayUserMenu(Scanner sc) {
        System.out.println("\n=============================");
        System.out.println("Welcome back, " + currentUser + "!");
        System.out.println("=============================");
        System.out.println("User Menu");
        System.out.println("1. Food Item Management");
        System.out.println("2. Meal Logging");
        System.out.println("3. Logout");
        System.out.print("Choice: ");
        int n = sc.nextInt();
        sc.nextLine();

        switch (n) {
            case 1:
                displayFoodItemMenu(sc);
                break;
            case 2:
                createMeal(sc);
                break;
            case 3:
                isLoggedIn = false;
                currentUser = "";
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    public static void displayFoodItemMenu(Scanner sc) {
        System.out.println("\nFood Item Management");
        System.out.println("1. Search Food Item");
        System.out.println("2. Add Food Item");
        System.out.println("3. Back to User Menu");
        System.out.print("Choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter the food item you want to search: ");
                String foodItem = sc.nextLine();
                FoodItems.SearchFood(foodItem);
                break;
            case 2:
                addFoodItem(sc);
                break;
            case 3:
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    public static void addFoodItem(Scanner sc) {
        System.out.println("\nAdd Food Item");
        System.out.print("Enter the name of the food item: ");
        String name = sc.nextLine();
        System.out.print("Enter the calories of the food item: ");
        int calories = sc.nextInt();
        System.out.print("Enter the serving size of the food item: ");
        int servingSize = sc.nextInt();

        FoodItems.addFoodItem(name, calories, servingSize);
        System.out.println("Food item added to the inventory.");
    }

    public static void createMeal(Scanner sc) {
        System.out.println("\nMeal Logging");
        System.out.println("Select meal type: ");
        System.out.println("1. Custom Meal");
        System.out.println("2. Predefined Meal");
        System.out.print("Choice: ");
        int mealType = sc.nextInt();
        sc.nextLine();

        Meal meal;
        switch (mealType) {
            case 1:
                meal = new CustomMeal();
                break;
            case 2:
                meal = new PredefinedMeal();
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        meal.createMeal(sc);
        System.out.println("Total calorie intake for the meal: " + meal.getTotalCalories());
    }
}

abstract class Meal {
    protected List<String> foodItems = new ArrayList<>();
    protected List<Integer> portionSizes = new ArrayList<>();
    protected int totalCalories = 0;

    public abstract void createMeal(Scanner sc);

    protected void displayMeal() {
        System.out.println("\nMeal:");
        for (int i = 0; i < foodItems.size(); i++) {
            System.out.println(foodItems.get(i) + ": " + portionSizes.get(i));
        }
    }

    protected int calculateCalories(String foodItem, int portionSize) {
        for (Map<String, Object> item : FoodTracker.Inventory) {
            if (item.get("name").toString().equalsIgnoreCase(foodItem.trim())) {
                int caloriesPerServing = Integer.parseInt(item.get("calories").toString());
                return caloriesPerServing * portionSize;
            }
        }
        return 0;
    }

    public int getTotalCalories() {
        return totalCalories;
    }
}

class CustomMeal extends Meal {
    @Override
    public void createMeal(Scanner sc) {
        System.out.println("\nSelect food items from the inventory (type 'done' when finished):");
        FoodItems.displayInventory();
        String input;
        while (!(input = sc.nextLine()).equalsIgnoreCase("done")) {
            foodItems.add(input);
            System.out.print("Enter portion size for " + input + ": ");
            int portionSize = sc.nextInt();
            portionSizes.add(portionSize);
            totalCalories += calculateCalories(input, portionSize);
            sc.nextLine();
        }
        displayMeal();
    }
}

class PredefinedMeal extends Meal {
    @Override
    public void createMeal(Scanner sc) {
        System.out.println("\nSelect a predefined meal:");
        System.out.println("Predefined meal selected.");
        totalCalories = 500;
        displayMeal();
    }
}

class Registration {
    public static List<Map<String, Object>> register_details() {
        List<Map<String, Object>> userList = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        System.out.println("\nRegistration");
        System.out.println("Enter the user details (name, age, weight, height) type exit to end the input");

        while (true) {
            System.out.print("Name: ");
            String name = sc.nextLine();
            if (name.equalsIgnoreCase("exit")) {
                break;
            }
            System.out.print("Age: ");
            int age = sc.nextInt();
            sc.nextLine();

            System.out.print("Weight: ");
            double weight = sc.nextDouble();
            sc.nextLine();

            System.out.print("Height: ");
            double height = sc.nextDouble();
            sc.nextLine();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("Name", name);
            userMap.put("Age", age);
            userMap.put("Weight", weight);
            userMap.put("Height", height);

            userList.add(userMap);

            System.out.println("\n Your Information: ");
            for (Map<String, Object> user : userList) {
                System.out.println("Name: " + user.get("Name"));
                System.out.println("Age: " + user.get("Age"));
                System.out.println("Weight: " + user.get("Weight"));
                System.out.println("Height: " + user.get("Height"));
            }
        }
        return userList;
    }
}

class FoodItems {
    public static List<Map<String, Object>> Inventory = new ArrayList<>();

    public static void FoodTracker() {
        Map<String, Object> foodItem1 = new HashMap<>();
        foodItem1.put("name", "Apple");
        foodItem1.put("calories", 95);
        foodItem1.put("servingSize", 1);
        Inventory.add(foodItem1);

        Map<String, Object> foodItem2 = new HashMap<>();
        foodItem2.put("name", "Orange");
        foodItem2.put("calories", 90);
        foodItem2.put("servingSize", 1);
        Inventory.add(foodItem2);

        Map<String, Object> foodItem3 = new HashMap<>();
        foodItem3.put("name", "PeanutButter");
        foodItem3.put("calories", 125);
        foodItem3.put("servingSize", 1);
        Inventory.add(foodItem3);
    }

    public static void SearchFood(String name) {
        boolean found = false;
        for (Map<String, Object> item : Inventory) {
            if (item.get("name").toString().equalsIgnoreCase(name.trim())) {
                System.out.println("\nName: " + item.get("name"));
                System.out.println("Calories: " + item.get("calories"));
                System.out.println("Serving Size: " + item.get("servingSize"));
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Your meal plan does not have this.");
        }
    }

    public static void addFoodItem(String name, int calories, int servingSize) {
        Map<String, Object> foodItem = new HashMap<>();
        foodItem.put("name", name);
        foodItem.put("calories", calories);
        foodItem.put("servingSize", servingSize);
        Inventory.add(foodItem);
    }

    public static void displayInventory() {
        System.out.println("\nInventory:");
        for (Map<String, Object> item : Inventory) {
            System.out.println(item.get("name") + " - Calories: " + item.get("calories") +
                    ", Serving Size: " + item.get("servingSize"));
        }
    }
}

class DatabaseHelper {
    private Connection conn;

    public DatabaseHelper(Connection conn) {
        this.conn = conn;
    }

    public void addMotivationalQuote(String quote) throws SQLException {
        String sql = "INSERT INTO motivational_quotes (quote) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, quote);
            pstmt.executeUpdate();
            System.out.println("Motivational diet quote added to the database.");
        }
    }

    public List<String> getAllMotivationalQuotes() throws SQLException {
        List<String> quotes = new ArrayList<>();
        String sql = "SELECT quote FROM motivational_quotes";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String quote = rs.getString("quote");
                quotes.add(quote);
            }
        }
        return quotes;
    }
}

