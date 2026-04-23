package com.vcs.vcs_server.controller;

import com.vcs.vcs_server.model.Role;
import com.vcs.vcs_server.model.User;
import com.vcs.vcs_server.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User newUser) {
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Потребител с това име вече съществува!");
        }
        // Хешираме паролата преди запис
        newUser.setPasswordHash(BCrypt.hashpw(newUser.getPasswordHash(), BCrypt.gensalt()));
        userRepository.save(newUser);
        return ResponseEntity.ok("Успешна регистрация!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("Потребителят е изтрит.");
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestParam Role newRole) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok("Ролята е обновена.");
    }
}