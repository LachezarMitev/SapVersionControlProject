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

    // Този метод ще се изпълни автоматично при стартиране на сървъра
    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository) {
        return args -> {
            // Проверяваме дали вече има админ, за да не го създаваме два пъти
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                // Криптираме паролата 'admin123'
                admin.setPasswordHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
                admin.setRole(Role.ADMIN);

                // Важно: Задаваме текуща дата, за да избегнем грешката '0000-00-00'
                // Ако в модела ти User няма created_at, просто изтрий долния ред
                // admin.setCreatedAt(LocalDateTime.now());

                userRepository.save(admin);
                System.out.println("--------------------------------------");
                System.out.println("СЪОБЩЕНИЕ: Базата беше празна. ");
                System.out.println("Създаден е служебен админ: admin / admin123");
                System.out.println("--------------------------------------");
            }
        };
    }
}