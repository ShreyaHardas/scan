package codetoscan;

import java.util.Random;
import java.sql.*;

public class SampleVulnerableClass {
    public void login(String username, String password) throws Exception {
        String adminPassword = "secret123"; // Hardcoded password
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "user", "pass");
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        stmt.executeQuery(query);
    }

    public void generateToken() {
        Random rand = new Random(); // Insecure random
        System.out.println("Token: " + rand.nextInt());
    }
}
