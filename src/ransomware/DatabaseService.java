package ransomware;

import java.sql.*;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:alerts.db";

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS alerts (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "timestamp TEXT NOT NULL, " +
                                    "message TEXT NOT NULL)";
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.out.println("[DatabaseService] Initialization error: " + e.getMessage());
        }
    }

    public static void saveAlert(String message) {
        String insertSQL = "INSERT INTO alerts(timestamp, message) VALUES(datetime('now'), ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, message);
            pstmt.executeUpdate();
            System.out.println("[DatabaseService] Alert saved to database.");
        } catch (SQLException e) {
            System.out.println("[DatabaseService] Failed to save alert: " + e.getMessage());
        }
    }
}


