package com.vcs.vcs_server;

import com.vcs.vcs_server.model.Role;
import com.vcs.vcs_server.model.User;
import com.vcs.vcs_server.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class VcsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VcsServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
                admin.setRole(Role.ADMIN);


                userRepository.save(admin);
                System.out.println("--------------------------------------");
                System.out.println("СЪОБЩЕНИЕ: Базата беше празна. ");
                System.out.println("Създаден е служебен админ: admin / admin123");
                System.out.println("--------------------------------------");
            }
        };
    }
}