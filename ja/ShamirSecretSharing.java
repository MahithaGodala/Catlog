import org.json.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecretSharing {
    public static void main(String[] args) {
        try {
            // Process the test case from the JSON file (input.json)
            System.out.println("Processing input.json...");
            processTestCase("input.json");

            // Process another test case (input1.json)
            System.out.println("Processing input1.json...");
            processTestCase("input1.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to process each test case
    private static void processTestCase(String jsonFilePath) throws IOException {
        // Parse JSON file
        JSONObject jsonData = new JSONObject(readFile(jsonFilePath));
        
        // Extract keys
        JSONObject keys = jsonData.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");
        
        // Decode roots (points)
        List<Point> points = new ArrayList<>();
        for (String key : jsonData.keySet()) {
            if (!key.equals("keys")) {
                JSONObject point = jsonData.getJSONObject(key);
                int x = Integer.parseInt(key); // x-coordinate
                int base = point.getInt("base");
                String value = point.getString("value");
                BigInteger y = new BigInteger(value, base); // Decode y-coordinate
                points.add(new Point(x, y));
            }
        }

        // Ensure we have enough points
        if (points.size() < k) {
            throw new IllegalArgumentException("Insufficient number of points.");
        }

        // Solve polynomial using Gaussian elimination
        BigInteger secret = solveUsingGaussianElimination(points.subList(0, k));
        System.out.println("The secret (constant term) for " + jsonFilePath + " is: " + secret);
    }

    // Function to solve for the secret using Gaussian elimination
    private static BigInteger solveUsingGaussianElimination(List<Point> points) {
        int n = points.size();
        BigInteger[][] matrix = new BigInteger[n][n];
        BigInteger[] results = new BigInteger[n];

        // Set up the matrix and result vector
        for (int i = 0; i < n; i++) {
            Point point = points.get(i);
            int x = point.x;
            BigInteger y = point.y;

            // Set up the coefficients for the polynomial terms
            for (int j = 0; j < n; j++) {
                matrix[i][j] = BigInteger.valueOf((long) Math.pow(x, j));
            }

            // Set up the result vector
            results[i] = y;
        }

        // Perform Gaussian elimination to solve the system
        for (int i = 0; i < n; i++) {
            // Make the diagonal element 1 (pivot)
            BigInteger pivot = matrix[i][i];
            for (int j = 0; j < n; j++) {
                matrix[i][j] = matrix[i][j].divide(pivot);
            }
            results[i] = results[i].divide(pivot);

            // Eliminate the other rows
            for (int j = i + 1; j < n; j++) {
                BigInteger factor = matrix[j][i];
                for (int k = 0; k < n; k++) {
                    matrix[j][k] = matrix[j][k].subtract(matrix[i][k].multiply(factor));
                }
                results[j] = results[j].subtract(results[i].multiply(factor));
            }
        }

        // Back substitution to solve for the coefficients
        BigInteger[] solution = new BigInteger[n];
        for (int i = n - 1; i >= 0; i--) {
            solution[i] = results[i];
            for (int j = i + 1; j < n; j++) {
                solution[i] = solution[i].subtract(matrix[i][j].multiply(solution[j]));
            }
        }

        // The secret is the constant term (c_0), which is the first coefficient
        return solution[0];
    }

    // Helper to read file content
    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileReader reader = new FileReader(filePath)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                content.append((char) ch);
            }
        }
        return content.toString();
    }

    // Point class to store x, y pairs
    static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}
