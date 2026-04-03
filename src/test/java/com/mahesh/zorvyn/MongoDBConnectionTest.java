package com.mahesh.zorvyn;

import com.mahesh.zorvyn.model.FinancialRecord;
import com.mahesh.zorvyn.model.RecordType;
import com.mahesh.zorvyn.model.Role;
import com.mahesh.zorvyn.model.User;
import com.mahesh.zorvyn.repository.FinancialRecordRepository;
import com.mahesh.zorvyn.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MongoDBConnectionTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialRecordRepository financialRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testMongoDBConnection() {
        System.out.println("\n========================================");
        System.out.println("Testing MongoDB Connection...");
        System.out.println("========================================\n");

        try {
            // Test 1: Check if repositories are initialized
            assertNotNull(userRepository, "UserRepository should not be null");
            assertNotNull(financialRecordRepository, "FinancialRecordRepository should not be null");
            System.out.println("Repositories initialized successfully");

            // Test 2: Count existing users
            long userCount = userRepository.count();
            System.out.println("Current users in database: " + userCount);

            // Test 3: Count existing records
            long recordCount = financialRecordRepository.count();
            System.out.println("Current financial records in database: " + recordCount);

            // Test 4: Create a test user
            String testUsername = "testuser_" + System.currentTimeMillis();
            User testUser = new User();
            testUser.setUsername(testUsername);
            testUser.setEmail(testUsername + "@test.com");
            testUser.setPassword(passwordEncoder.encode("Test@123456"));
            testUser.setRoles(Set.of(Role.ROLE_USER));

            User savedUser = userRepository.save(testUser);
            assertNotNull(savedUser.getId(), "User ID should not be null after save");
            System.out.println("Test user created successfully: " + savedUser.getUsername());
            System.out.println("  User ID: " + savedUser.getId());

            // Test 5: Create test financial records
            System.out.println("\n--- Creating Test Financial Records ---");

            // Income record 1
            FinancialRecord income1 = new FinancialRecord();
            income1.setUserId(savedUser.getId());
            income1.setType(RecordType.INCOME);
            income1.setAmount(5000.00);
            income1.setCategory("Salary");
            income1.setDescription("Monthly salary - January");
            income1.setDate(LocalDate.now().minusDays(30));
            FinancialRecord savedIncome1 = financialRecordRepository.save(income1);
            System.out.println("Income record 1 created: $" + savedIncome1.getAmount() + " - " + savedIncome1.getCategory());

            // Income record 2
            FinancialRecord income2 = new FinancialRecord();
            income2.setUserId(savedUser.getId());
            income2.setType(RecordType.INCOME);
            income2.setAmount(1500.00);
            income2.setCategory("Freelance");
            income2.setDescription("Freelance project payment");
            income2.setDate(LocalDate.now().minusDays(15));
            FinancialRecord savedIncome2 = financialRecordRepository.save(income2);
            System.out.println("Income record 2 created: $" + savedIncome2.getAmount() + " - " + savedIncome2.getCategory());

            // Expense record 1
            FinancialRecord expense1 = new FinancialRecord();
            expense1.setUserId(savedUser.getId());
            expense1.setType(RecordType.EXPENSE);
            expense1.setAmount(1200.00);
            expense1.setCategory("Rent");
            expense1.setDescription("Monthly rent payment");
            expense1.setDate(LocalDate.now().minusDays(25));
            FinancialRecord savedExpense1 = financialRecordRepository.save(expense1);
            System.out.println("Expense record 1 created: $" + savedExpense1.getAmount() + " - " + savedExpense1.getCategory());

            // Expense record 2
            FinancialRecord expense2 = new FinancialRecord();
            expense2.setUserId(savedUser.getId());
            expense2.setType(RecordType.EXPENSE);
            expense2.setAmount(300.00);
            expense2.setCategory("Groceries");
            expense2.setDescription("Weekly grocery shopping");
            expense2.setDate(LocalDate.now().minusDays(10));
            FinancialRecord savedExpense2 = financialRecordRepository.save(expense2);
            System.out.println("Expense record 2 created: $" + savedExpense2.getAmount() + " - " + savedExpense2.getCategory());

            // Expense record 3
            FinancialRecord expense3 = new FinancialRecord();
            expense3.setUserId(savedUser.getId());
            expense3.setType(RecordType.EXPENSE);
            expense3.setAmount(150.00);
            expense3.setCategory("Utilities");
            expense3.setDescription("Electricity bill");
            expense3.setDate(LocalDate.now().minusDays(5));
            FinancialRecord savedExpense3 = financialRecordRepository.save(expense3);
            System.out.println("Expense record 3 created: $" + savedExpense3.getAmount() + " - " + savedExpense3.getCategory());

            // Test 6: Retrieve all records for the user
            List<FinancialRecord> userRecords = financialRecordRepository.findByUserId(savedUser.getId());
            assertEquals(5, userRecords.size(), "Should have 5 records");
            System.out.println("\nRetrieved " + userRecords.size() + " records for user");

            // Test 7: Calculate totals
            double totalIncome = userRecords.stream()
                    .filter(r -> r.getType() == RecordType.INCOME)
                    .mapToDouble(FinancialRecord::getAmount)
                    .sum();

            double totalExpense = userRecords.stream()
                    .filter(r -> r.getType() == RecordType.EXPENSE)
                    .mapToDouble(FinancialRecord::getAmount)
                    .sum();

            double balance = totalIncome - totalExpense;

            System.out.println("\n--- Financial Summary ---");
            System.out.println("Total Income:  $" + String.format("%.2f", totalIncome));
            System.out.println("Total Expense: $" + String.format("%.2f", totalExpense));
            System.out.println("Balance:       $" + String.format("%.2f", balance));

            // Test 8: Verify user can be found
            User foundUser = userRepository.findByUsername(testUsername).orElse(null);
            assertNotNull(foundUser, "User should be found by username");
            assertEquals(testUsername, foundUser.getUsername());
            System.out.println("\nUser retrieved successfully by username");

            // Test 9: Clean up - Delete test data
            System.out.println("\n--- Cleaning Up Test Data ---");
            financialRecordRepository.deleteAll(userRecords);
            System.out.println("Deleted " + userRecords.size() + " financial records");

            userRepository.delete(savedUser);
            System.out.println("Deleted test user");

            System.out.println("\n========================================");
            System.out.println("All MongoDB Tests Passed Successfully!");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("\n✗ MongoDB Connection Test Failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            fail("MongoDB connection test failed: " + e.getMessage());
        }
    }

    @Test
    public void testCreateSampleData() {
        System.out.println("\n========================================");
        System.out.println("Creating Sample Data for Testing...");
        System.out.println("========================================\n");

        try {
            // Create sample user
            String sampleUsername = "sampleuser";
            
            // Check if user already exists
            if (userRepository.findByUsername(sampleUsername).isPresent()) {
                System.out.println("⚠ Sample user already exists, skipping creation");
                return;
            }

            User sampleUser = new User();
            sampleUser.setUsername(sampleUsername);
            sampleUser.setEmail("sample@example.com");
            sampleUser.setPassword(passwordEncoder.encode("Sample@123"));
            sampleUser.setRoles(Set.of(Role.ROLE_USER));

            User savedUser = userRepository.save(sampleUser);
            System.out.println("Sample user created");
            System.out.println("  Username: " + savedUser.getUsername());
            System.out.println("  Password: Sample@123");
            System.out.println("  User ID: " + savedUser.getId());

            // Create diverse sample records
            createSampleRecord(savedUser.getId(), RecordType.INCOME, 5000.00, "Salary", "Monthly salary", 30);
            createSampleRecord(savedUser.getId(), RecordType.INCOME, 2000.00, "Freelance", "Web development project", 20);
            createSampleRecord(savedUser.getId(), RecordType.INCOME, 500.00, "Investment", "Stock dividends", 15);
            
            createSampleRecord(savedUser.getId(), RecordType.EXPENSE, 1500.00, "Rent", "Monthly rent", 28);
            createSampleRecord(savedUser.getId(), RecordType.EXPENSE, 400.00, "Groceries", "Monthly groceries", 25);
            createSampleRecord(savedUser.getId(), RecordType.EXPENSE, 200.00, "Utilities", "Electricity and water", 22);
            createSampleRecord(savedUser.getId(), RecordType.EXPENSE, 150.00, "Transportation", "Gas and maintenance", 18);
            createSampleRecord(savedUser.getId(), RecordType.EXPENSE, 100.00, "Entertainment", "Movies and dining", 10);
            createSampleRecord(savedUser.getId(), RecordType.EXPENSE, 80.00, "Healthcare", "Pharmacy", 8);
            createSampleRecord(savedUser.getId(), RecordType.EXPENSE, 50.00, "Subscriptions", "Netflix and Spotify", 5);

            List<FinancialRecord> allRecords = financialRecordRepository.findByUserId(savedUser.getId());
            System.out.println("\nCreated " + allRecords.size() + " sample financial records");

            // Calculate summary
            double totalIncome = allRecords.stream()
                    .filter(r -> r.getType() == RecordType.INCOME)
                    .mapToDouble(FinancialRecord::getAmount)
                    .sum();

            double totalExpense = allRecords.stream()
                    .filter(r -> r.getType() == RecordType.EXPENSE)
                    .mapToDouble(FinancialRecord::getAmount)
                    .sum();

            System.out.println("\n--- Sample Data Summary ---");
            System.out.println("Total Income:  $" + String.format("%.2f", totalIncome));
            System.out.println("Total Expense: $" + String.format("%.2f", totalExpense));
            System.out.println("Net Balance:   $" + String.format("%.2f", (totalIncome - totalExpense)));

            System.out.println("\n========================================");
            System.out.println("Sample Data Created Successfully!");
            System.out.println("========================================\n");

        } catch (Exception e) {
            System.err.println("\n✗ Sample Data Creation Failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSampleRecord(String userId, RecordType type, double amount, 
                                   String category, String description, int daysAgo) {
        FinancialRecord record = new FinancialRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setAmount(amount);
        record.setCategory(category);
        record.setDescription(description);
        record.setDate(LocalDate.now().minusDays(daysAgo));
        
        FinancialRecord saved = financialRecordRepository.save(record);
        System.out.println("  " + type + ": $" + amount + " - " + category);
    }
}
