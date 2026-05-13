package com.tokenplatform.config;

import com.tokenplatform.model.User;
import com.tokenplatform.model.UserRole;
import com.tokenplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDisplayName("管理员");
            admin.setEmail("admin@toolbase.com");
            admin.setBalance(99999999L); // 999,999.99 yuan for admin
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("✅ Admin user created: admin / admin123");
        }

        if (!userRepository.findByUsername("demo").isPresent()) {
            User demo = new User();
            demo.setUsername("demo");
            demo.setPassword(passwordEncoder.encode("demo123"));
            demo.setDisplayName("Demo User");
            demo.setEmail("demo@toolbase.com");
            demo.setBalance(500L); // 5 yuan free credit
            demo.setRole(UserRole.USER);
            demo.setEnabled(true);
            userRepository.save(demo);
            log.info("✅ Demo user created: demo / demo123");
        }

        log.info("📊 Data initialization complete");
    }
}
