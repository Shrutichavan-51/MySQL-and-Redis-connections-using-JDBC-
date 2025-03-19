import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import redis.clients.jedis.Jedis;

/**
 * This program connects to a MySQL database and inserts user data into the 'users' table.
 * Additionally, it stores the same user data in a Redis database.
 * Features:
 * - Establishes a connection to MySQL using JDBC
 * - Inserts user data into the 'users' table
 * - Connects to Redis and stores the same user data as a hash
 * - Implements exception handling for database and Redis operations
 */

public class InsertData1 {
    public static void main(String[] args) {
        // step 1: Database connection 
        String url = "jdbc:mysql://localhost:3306/test_db";
        String user = "root"; 
        String password = "5151"; 

        // step 2: SQL query (without `id`, assuming it's AUTO_INCREMENT)
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";

        try {
            //step 3:  Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver class loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not loaded");
            e.printStackTrace();
        }
             // step 4: Establish connection to MySQL
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameter values
            pstmt.setString(1, "Alice Smith");
            pstmt.setString(2, "alice@example.com");

            // step 5: Execute insert statement
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println(rowsInserted + " records inserted successfully!");
            } else {
                System.out.println("No records inserted!");
            }

            // Redis operation
            try (Jedis jedis = new Jedis("localhost", 6379)) { 
                System.out.println("Connected to Redis...");
                jedis.hset("user:1", "name", "Alice Smith");
                jedis.hset("user:1", "email", "alice@example.com");

                System.out.println("Data stored in Redis successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
