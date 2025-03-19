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

            // Initialize Redis Connection
            try (Jedis jedis = new Jedis(redisHost, redisPort);
                 Connection conn = DriverManager.getConnection(url, user, password);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                System.out.println("Connected to Redis and MySQL!");

                // Measure MySQL Query Time
                long mysqlStartTime = System.currentTimeMillis();

                // Fetch 100 records from MySQL
                for (int i = 0; i < 100; i++) {
                    int randomId = random.nextInt(1000) + 1;
                    pstmt.setInt(1, randomId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String studentData = "Student ID: " + rs.getInt("id") +
                                    ", Name: " + rs.getString("name") +
                                    ", Type: " + rs.getString("hostler_or_dayscholar") +
                                    ", Mobile: " + rs.getString("mobile") +
                                    ", Email: " + rs.getString("email");

                            System.out.println(studentData);

                            // Store data in Redis with a 1-hour expiration time
                            jedis.setex("student:" + randomId, 3600, studentData);
                        } else {
                            System.out.println("Student with ID " + randomId + " not found.");
                        }
                    }
                }

                long mysqlEndTime = System.currentTimeMillis();
                System.out.println("MySQL Fetch Time: " + (mysqlEndTime - mysqlStartTime) + " ms");

                // Measure Redis Query Time
                long redisStartTime = System.currentTimeMillis();
                long totalRedisFetchTime = 0;

                // Fetch the same 100 records from Redis
                for (int i = 0; i < 100; i++) {
                    int randomId = random.nextInt(1000) + 1;
                    String cacheKey = "student:" + randomId;

                    long singleStartTime = System.currentTimeMillis();
                    if (jedis.exists(cacheKey)) {
                        String data = jedis.get(cacheKey);
                        long singleEndTime = System.currentTimeMillis();
                        long fetchTime = singleEndTime - singleStartTime;
                        totalRedisFetchTime += fetchTime;
                        System.out.println("From Redis: " + data);
                        System.out.println("Redis Query for ID " + randomId + " executed in " + fetchTime + " ms");
                    } else {
                        System.out.println("Data not found in Redis for Student ID: " + randomId);
                    }
                }

                long redisEndTime = System.currentTimeMillis();
                System.out.println("Redis Total Fetch Time: " + (redisEndTime - redisStartTime) + " ms");
                System.out.println("Calculated Total Redis Fetch Time (Summed): " + totalRedisFetchTime + " ms");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}