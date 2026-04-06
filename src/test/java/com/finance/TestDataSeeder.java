package com.finance;

import com.finance.entity.Role;
import com.finance.entity.User;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a test admin user before tests run.
 * The main DataSeeder is skipped in test profile.
 */
@Component
public class TestDataSeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void seedAdmin() {
        if (userRepository.existsByEmail("testadmin@finance.com")) return;
        userRepository.save(User.builder()
                .name("Test Admin")
                .email("testadmin@finance.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(Role.ADMIN)
                .build());
    }
}
