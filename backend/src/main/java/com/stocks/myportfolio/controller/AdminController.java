package com.stocks.myportfolio.controller;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.dto.response.auth.UserResponse;
import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return toResponse(findUser(id));
    }

    @PutMapping("/users/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = findUser(id);
        String status = body.get("status");
        if (status != null && List.of("ACTIVE", "INACTIVE", "SUSPENDED").contains(status)) {
            user.setStatus(status);
            userRepository.save(user);
        }
        return toResponse(user);
    }

    @PostMapping("/users/{id}/reset-password")
    public Map<String, String> resetPassword(@PathVariable Long id) {
        User user = findUser(id);
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        String special = "@#$%&*!";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        sb.insert(4, special.charAt(random.nextInt(special.length())));
        sb.insert(10, random.nextInt(10));
        String tempPassword = sb.toString();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        return Map.of("message", "Password reset", "temporaryPassword", tempPassword);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id) {
        User user = findUser(id);
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return Map.of("error", "Cannot delete admin user");
        }
        userRepository.delete(user);
        return Map.of("message", "User deleted: " + user.getEmail());
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getPhone(), user.getRole(), user.getStatus(),
                user.isEmailVerified());
    }
}
