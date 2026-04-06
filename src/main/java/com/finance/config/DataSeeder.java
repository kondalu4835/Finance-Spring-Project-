package com.finance.config;

import com.finance.entity.*;
import com.finance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    @Profile("!test")
    public CommandLineRunner seedData(UserRepository userRepo,
                                     FinancialRecordRepository recordRepo,
                                     PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() > 0) {
                log.info("Database already seeded — skipping.");
                return;
            }

            // Create users
            User admin = userRepo.save(User.builder()
                    .name("Alice Admin").email("admin@finance.dev")
                    .password(encoder.encode("Admin@1234")).role(Role.ADMIN).build());

            User analyst = userRepo.save(User.builder()
                    .name("Bob Analyst").email("analyst@finance.dev")
                    .password(encoder.encode("Analyst@1234")).role(Role.ANALYST).build());

            userRepo.save(User.builder()
                    .name("Carol Viewer").email("viewer@finance.dev")
                    .password(encoder.encode("Viewer@1234")).role(Role.VIEWER).build());

            // Create financial records
            Object[][] records = {
                {new BigDecimal("5000"), RecordType.INCOME,  "Salary",     LocalDate.of(2024,1,5),  "January salary"},
                {new BigDecimal("200"),  RecordType.EXPENSE, "Utilities",  LocalDate.of(2024,1,10), "Electric bill"},
                {new BigDecimal("1500"), RecordType.INCOME,  "Freelance",  LocalDate.of(2024,1,15), "Design project"},
                {new BigDecimal("350"),  RecordType.EXPENSE, "Groceries",  LocalDate.of(2024,1,20), "Weekly shop"},
                {new BigDecimal("5000"), RecordType.INCOME,  "Salary",     LocalDate.of(2024,2,5),  "February salary"},
                {new BigDecimal("800"),  RecordType.EXPENSE, "Rent",       LocalDate.of(2024,2,1),  "Monthly rent"},
                {new BigDecimal("120"),  RecordType.EXPENSE, "Transport",  LocalDate.of(2024,2,14), "Fuel"},
                {new BigDecimal("2000"), RecordType.INCOME,  "Investment", LocalDate.of(2024,3,1),  "Dividend"},
                {new BigDecimal("450"),  RecordType.EXPENSE, "Healthcare", LocalDate.of(2024,3,8),  "Doctor visit"},
                {new BigDecimal("5000"), RecordType.INCOME,  "Salary",     LocalDate.of(2024,3,5),  "March salary"},
            };

            for (Object[] r : records) {
                recordRepo.save(FinancialRecord.builder()
                        .amount((BigDecimal) r[0])
                        .type((RecordType) r[1])
                        .category((String) r[2])
                        .date((LocalDate) r[3])
                        .notes((String) r[4])
                        .createdBy(admin)
                        .build());
            }

            log.info("✅ Seed complete: admin@finance.dev / Admin@1234");
            log.info("✅ Seed complete: analyst@finance.dev / Analyst@1234");
            log.info("✅ Seed complete: viewer@finance.dev / Viewer@1234");
        };
    }
}
