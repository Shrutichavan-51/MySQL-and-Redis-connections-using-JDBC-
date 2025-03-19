import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import redis.clients.jedis.Jedis;

public class FetchDataMySQL {
    public static void main(String[] args) {
        // Database and Redis Configuration
        String url = "jdbc:mysql://localhost:3306/test_db";
        String user = "root";
        String password = "5151";
        String redisHost = "localhost";
        int redisPort = 6379;

        String sql = "SELECT * FROM students WHERE id = ?";
        Random random = new Random();

        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded successfully!");

            // Initialize Redis and MySQL connections
            try (Jedis jedis = new Jedis(redisHost, redisPort);
                 Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                System.out.println("Connected to Redis and MySQL!");

                long totalMySQLFetchTime = 0;
                long totalRedisFetchTime = 0;

                // Fetch 100 records from random locations
                for (int i = 0; i < 100; i++) {
                    int randomId = random.nextInt(1000) + 1;
                    String cacheKey = "student:" + randomId;

                    // **Measure MySQL Query Time**
                    long mysqlStartTime = System.currentTimeMillis();
                    pstmt.setInt(1, randomId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String studentData = "Student ID: " + rs.getInt("id") +
                                    ", Name: " + rs.getString("name") +
                                    ", Type: " + rs.getString("hostler_or_dayscholar") +
                                    ", Mobile: " + rs.getString("mobile") +
                                    ", Email: " + rs.getString("email");

                            long mysqlEndTime = System.currentTimeMillis();
                            long mysqlFetchTime = mysqlEndTime - mysqlStartTime;
                            totalMySQLFetchTime += mysqlFetchTime;

                            System.out.println("From MySQL: " + studentData);
                            System.out.println("MySQL Query for ID " + randomId + " executed in " + mysqlFetchTime + " ms");

                            // Store data in Redis with a 1-hour expiration time
                            jedis.setex(cacheKey, 3600, studentData);
                        } else {
                            System.out.println("Student with ID " + randomId + " not found in MySQL.");
                        }
                    }

                    // **Measure Redis Query Time**
                    long redisStartTime = System.currentTimeMillis();
                    if (jedis.exists(cacheKey)) {
                        String cachedData = jedis.get(cacheKey);
                        long redisEndTime = System.currentTimeMillis();
                        long redisFetchTime = redisEndTime - redisStartTime;
                        totalRedisFetchTime += redisFetchTime;

                        System.out.println("From Redis: " + cachedData);
                        System.out.println("Redis Query for ID " + randomId + " executed in " + redisFetchTime + " ms");
                    } else {
                        System.out.println("Data not found in Redis for Student ID: " + randomId);
                    }
                }

                System.out.println("Total MySQL Fetch Time: " + totalMySQLFetchTime + " ms");
                System.out.println("Total Redis Fetch Time: " + totalRedisFetchTime + " ms");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
