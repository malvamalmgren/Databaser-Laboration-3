import java.sql.*;
import java.util.Scanner;

private static final Scanner scanner = new Scanner(System.in);

public static void main(String[] args) {
    boolean quit = false;
    printActions();

    while (!quit) {
        System.out.println("\nChoose an option (5 to show options):");
        String action = scanner.next();

        switch (action) {
            case "0" -> {
                System.out.println("\nClosing program...");
                quit = true;
            }
            case "1" -> searchMenu();
            case "2" -> managePetsMenu();
            case "3" -> manageSpeciesMenu();
            case "4" -> favoritesMenu();
            case "5" -> printActions();
            default -> System.out.println("Invalid option. Please try again.");
        }
    }
}

private static void printActions() {
    System.out.println("\nMain menu:\n");
    System.out.println("""
            0 - Close
            1 - Search...
            2 - Manage Pets...
            3 - Manage Species...
            4 - Favorites...
            5 - Show options.""");
}


private static Connection connect() {
    String url = "jdbc:sqlite:db/JU24_MalvaMalmgren_Laboration3.db";
    Connection conn = null;
    try {
        conn = DriverManager.getConnection(url);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return conn;
}

public static void searchMenu() {
    System.out.println("\nSearch:");
    System.out.println("""
            1 - Show all pets
            2 - Show favorites
            3 - Show statistics
            4 - Search by name
            5 - Search by species
            6 - Back to main menu
            """);

    System.out.print("Choose an action: ");
    String action = scanner.next();
    scanner.nextLine();

    switch (action) {
        case "1" -> showAll();
        case "2" -> showFavorites();
        case "3" -> showStatistics();
        case "4" -> nameSearch();
        case "5" -> speciesSearch();
        case "6" -> System.out.println("Returning to main menu...");
        default -> System.out.println("Invalid option, returning to main menu...");
    }
}

public static void managePetsMenu() {
    System.out.println("\nManage pets:");
    System.out.println("""
            1 - Add a pet
            2 - Update a pet
            3 - Delete a pet
            4 - Back to main menu
            """);

    System.out.print("Choose an action: ");
    String action = scanner.next();
    scanner.nextLine();

    switch (action) {
        case "1" -> inputPetInsert();
        case "2" -> inputPetUpdate();
        case "3" -> inputPetDelete();
        case "4" -> System.out.println("Returning to main menu...");
        default -> System.out.println("Invalid option, returning to main menu...");
    }
}

public static void manageSpeciesMenu() {
    System.out.println("\nManage species:");
    System.out.println("""
            1 - Add a species
            2 - Back to main menu
            """);

    System.out.print("Choose an action: ");
    String action = scanner.next();
    scanner.nextLine();

    switch (action) {
        case "1" -> inputSpeciesInsert();
        case "2" -> System.out.println("Returning to main menu...");
        default -> System.out.println("Invalid option, returning to main menu...");
    }
}

public static void favoritesMenu() {
    System.out.println("\nFavorites:");
    System.out.println("""
            1 - Add favorite
            2 - Show favorites
            3 - Remove favorite
            4 - Back to main menu
            """);

    System.out.print("Choose an action: ");
    String action = scanner.next();

    switch (action) {
        case "1" -> addFavorite();
        case "2" -> showFavorites();
        case "3" -> removeFavorite();
        case "4" -> System.out.println("Returning to main menu...");
        default -> System.out.println("Invalid option, returning to main menu...");
    }
}

private static void showAll() {
    String sql = "SELECT pet.petId, pet.petName, pet.petAge, pet.petBreed, species.speciesName " +
            "FROM pet " +
            "INNER JOIN species ON pet.petSpeciesId = species.speciesId " +
            "ORDER BY pet.petName";

    try {
        Connection conn = connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("petId") +
                    ", Name: " + rs.getString("petName") +
                    ", Age: " + rs.getInt("petAge") +
                    ", Breed: " + rs.getString("petBreed") +
                    ", Species: " + rs.getString("speciesName"));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void showStatistics() {
    String sql = "SELECT " +
            "(SELECT COUNT(*) FROM pet) AS petCount, " +
            "(SELECT COUNT(*) FROM species) AS speciesCount";
    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            System.out.println("Number of pets: " + rs.getInt("petCount"));
            System.out.println("Number of species: " + rs.getInt("speciesCount"));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void nameSearch() {
    String sql = "SELECT pet.petId, pet.petName, pet.petAge, pet.petBreed, species.speciesName " +
            "FROM pet " +
            "INNER JOIN species ON pet.petSpeciesId = species.speciesId " +
            "WHERE pet.petName LIKE ? ";

    try {
        Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        System.out.println("Enter pet name: ");
        String inputPetName = scanner.nextLine();

        pstmt.setString(1, "%" + inputPetName + "%");
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("petId") +
                    ", Name: " + rs.getString("petName") +
                    ", Age: " + rs.getInt("petAge") +
                    ", Breed: " + rs.getString("petBreed") +
                    ", Species: " + rs.getString("speciesName"));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void speciesSearch() {
    String sql = "SELECT pet.petId, pet.petName, pet.petAge, pet.petBreed, species.speciesName " +
            "FROM pet " +
            "INNER JOIN species ON pet.petSpeciesId = species.speciesId " +
            "WHERE species.speciesName = ? ";

    try {
        Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        System.out.println("Enter species name: ");
        String inputSpeciesName = scanner.nextLine();
        int speciesId = getSpeciesIdByName(inputSpeciesName);
        if (speciesId == -1) {
            System.out.println("No pets found for species: " + inputSpeciesName);
            return;
        }

        pstmt.setString(1, inputSpeciesName);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("petId") +
                    ", Name: " + rs.getString("petName") +
                    ", Age: " + rs.getInt("petAge") +
                    ", Breed: " + rs.getString("petBreed") +
                    ", Species: " + rs.getString("speciesName"));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void inputPetInsert() {
    System.out.println("Enter pet name: ");
    String inputName = scanner.nextLine();
    System.out.println("Enter age: ");
    int inputAge = scanner.nextInt();
    scanner.nextLine();
    System.out.println("Enter breed: ");
    String inputBreed = scanner.nextLine();
    System.out.println("Enter species: ");
    String inputSpeciesName = scanner.nextLine();
    // Hämta speciesId baserat på artens namn
    int speciesId = ensureSpeciesExists(inputSpeciesName);
    // Kontrollera om arten finns
    if (speciesId == -1) {
        return;
    }
    // Lägg till djuret
    petInsert(inputName, inputAge, inputBreed, speciesId);
}

private static void petInsert(String name, int age, String breed, int speciesId) {
    String sql = "INSERT INTO pet(petName, petAge, petBreed, petSpeciesId) " +
            "VALUES(?,?,?,?)";

    try {
        Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.setInt(2, age);
        pstmt.setString(3, breed);
        pstmt.setInt(4, speciesId);
        pstmt.executeUpdate();
        System.out.println("Pet added!");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void inputPetUpdate() {
    System.out.println("Enter the pet's ID: ");
    int inputId = scanner.nextInt();
    scanner.nextLine();

    String[] currentValues = getPetById(inputId);
    if (currentValues == null) {
        System.out.println("Could not find pet with ID: " + inputId);
        return;
    }
    System.out.println("Current name: " + currentValues[0] + ". Enter a new name (or leave blank): ");
    String inputName = scanner.nextLine();
    if (inputName.isEmpty()) {
        inputName = currentValues[0];
    }

    System.out.println("Current age: " + currentValues[1] + ". Enter new age (or leave blank): ");
    String inputStringAge = scanner.nextLine();
    int inputAge = inputStringAge.isEmpty() ? Integer.parseInt(currentValues[1]) : Integer.parseInt(inputStringAge);

    System.out.println("Current breed: " + currentValues[2] + ". Enter new breed (or leave blank): ");
    String inputBreed = scanner.nextLine();
    if (inputBreed.isEmpty()) {
        inputBreed = currentValues[2];
    }

    System.out.println("Current species: " + currentValues[3] + ". Enter new species (or leave blank): ");
    String inputSpeciesName = scanner.nextLine();
    int speciesId;
    if (inputSpeciesName.isEmpty()) {
        speciesId = Integer.parseInt(currentValues[4]);
    } else {
        speciesId = getSpeciesIdByName(inputSpeciesName);
        if (speciesId == -1) {
            System.out.println("Species '" + inputSpeciesName + "' does not exist, you must add it first.");
            return;
        }
    }
    petUpdate(inputName, inputAge, inputBreed, speciesId, inputId);
}

private static void petUpdate(String name, int age, String breed, int speciesId, int petId) {
    String sql = "UPDATE pet SET petName = ? , "
            + "petAge = ? , "
            + "petBreed = ? , "
            + "petSpeciesId = ? "
            + "WHERE petId = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, name);
        pstmt.setInt(2, age);
        pstmt.setString(3, breed);
        pstmt.setInt(4, speciesId);
        pstmt.setInt(5, petId);
        pstmt.executeUpdate();
        System.out.println("Pet updated!");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static String[] getPetById(int petId) {
    String sql = "SELECT petName, petAge, petBreed, species.speciesName, species.speciesId " +
            "FROM pet " +
            "INNER JOIN species ON pet.petSpeciesId = species.speciesId " +
            "WHERE petId = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, petId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return new String[]{
                    rs.getString("petName"),
                    String.valueOf(rs.getInt("petAge")),
                    rs.getString("petBreed"),
                    rs.getString("speciesName"),
                    String.valueOf(rs.getInt("speciesId"))
            };
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
    return null;
}

private static void inputPetDelete() {
    System.out.println("Enter the pet's ID: ");
    int inputId = scanner.nextInt();
    petDelete(inputId);
    scanner.nextLine();
}

private static void petDelete(int id) {
    String sql = "DELETE FROM pet WHERE petId = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        System.out.println("Pet removed!");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void inputSpeciesInsert() {
    System.out.println("Enter species name: ");
    String inputName = scanner.nextLine();
    speciesInsert(inputName);
}

private static void speciesInsert(String name) {
    String sql = "INSERT INTO species(speciesName) " +
            "VALUES(?)";

    try {
        Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.executeUpdate();
        System.out.println("Species added!");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static int getSpeciesIdByName(String speciesName) {
    String sql = "SELECT speciesId FROM species WHERE speciesName = ?";

    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, speciesName);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("speciesId");
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }

    return -1; // Returnera -1 om arten inte finns
}

private static int ensureSpeciesExists(String inputSpeciesName) {
    int speciesId = getSpeciesIdByName(inputSpeciesName); // Kolla om art finns

    if (speciesId == -1) { // Om art inte finns
        System.out.println("Species '" + inputSpeciesName + "' does not exist, would you like to add it now? (yes/no)");
        String response = scanner.nextLine();
        if (response.equalsIgnoreCase("yes")) {
            speciesInsert(inputSpeciesName); // Om 'yes', lägg till art
            speciesId = getSpeciesIdByName(inputSpeciesName);
        } else {
            System.out.println("Cancelling operation.");
            return -1; // Vid annat svar, returnera -1 för att indikera att operationen avbryts
        }
    }

    return speciesId;
}

private static void addFavorite() {
    System.out.println("Enter the pet's ID to mark as favorite: ");
    int petId = scanner.nextInt();
    scanner.nextLine();
    String sql = "UPDATE pet SET petFavorite = 1 WHERE petId = ?";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, petId);
        pstmt.executeUpdate();
        System.out.println("Pet marked as favorite!");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void removeFavorite() {
    System.out.println("Enter the pet's ID to remove as favorite: ");
    int petId = scanner.nextInt();
    scanner.nextLine();
    String sql = "UPDATE pet SET petFavorite = 0 WHERE petId = ?";
    try (Connection conn = connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, petId);
        pstmt.executeUpdate();
        System.out.println("Pet removed as favorite.");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

private static void showFavorites() {
    String sql = "SELECT petId, petName, petAge, petBreed, speciesName " +
            "FROM pet INNER JOIN species ON pet.petSpeciesId = species.speciesId " +
            "WHERE petFavorite = 1";
    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("petId") +
                    ", Name: " + rs.getString("petName") +
                    ", Age: " + rs.getInt("petAge") +
                    ", Breed: " + rs.getString("petBreed") +
                    ", Species: " + rs.getString("speciesName"));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}
