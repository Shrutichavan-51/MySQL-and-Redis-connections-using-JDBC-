import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import redis.clients.jedis.Jedis;
import java.util.Random;

public class InsertData {

    public static void main(String[] args) {
        // Step 1: Database connection details
        String url = "jdbc:mysql://localhost:3306/test_db";
        String user = "root";
        String password = "5151";

        // Step 2: SQL query for inserting data
        String sql = "INSERT INTO students (name, hostler_or_dayscholar, mobile, email) VALUES (?, ?, ?, ?)";

        // Step 3: Sample Data Generation
        String[] names = {"Priti", "Shreya", "Gajanan", "Bhakti", "Jiya", "Soham", "Payal", "Aaraadhya"};
        String[] status = {"Hostler", "Day Scholar"};
        Random rand = new Random();

        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver Loaded.");

            // Establish connection to MySQL and Redis
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 Jedis jedis = new Jedis("localhost", 6379)) {

                System.out.println("Connected to MySQL and Redis.");

                for (int i = 1; i <= 1000; i++) {
                    // Generate random data
                    String studentName = names[rand.nextInt(names.length)] + i;
                    String studentStatus = status[rand.nextInt(status.length)];
                    String mobile = "99999" + String.format("%05d", i);
                    String email = studentName.toLowerCase() + "@abc.com";

                    // Insert into MySQL
                    pstmt.setString(1, studentName);
                    pstmt.setString(2, studentStatus);
                    pstmt.setString(3, mobile);
                    pstmt.setString(4, email);
                    pstmt.executeUpdate();

                    // Insert into Redis
                    String key = "student:" + i;
                    jedis.hset(key, "name", studentName);
                    jedis.hset(key, "status", studentStatus);
                    jedis.hset(key, "mobile", mobile);
                    jedis.hset(key, "email", email);

                    System.out.println("Inserted Student " + i + " into MySQL and Redis.");
                }
                System.out.println("1000 records inserted successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
