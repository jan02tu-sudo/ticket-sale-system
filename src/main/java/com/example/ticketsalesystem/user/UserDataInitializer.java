package com.example.ticketsalesystem.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserDataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserDataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new User(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "ADMIN"
            ));
        }
        if (!userRepository.existsByUsername("user")) {
            userRepository.save(new User(
                    "user",
                    passwordEncoder.encode("user123"),
                    "USER"
            ));
        }
    }
}