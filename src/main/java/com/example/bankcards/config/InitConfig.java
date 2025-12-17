package com.example.bankcards.config;

import com.example.bankcards.config.properties.AdminProperties;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@ConditionalOnProperty(name = {"bankcards.admin-username", "bankcards.admin-password"})
@EnableConfigurationProperties(AdminProperties.class)
public class InitConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;

    public InitConfig(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      AdminProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            String username = properties.getAdminUsername();

            if (!userRepository.existsByUsername(username)) {
                User admin = new User();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(properties.getAdminPassword()));
                admin.setRole(User.Role.ROLE_ADMIN);
                admin.setFullName("Системный Администратор");

                userRepository.save(admin);
                log.info("Администратор успешно создан.");
            } else {
                log.info("Администратор {} уже существует в базе данных.", username);
            }
        };
    }
}
