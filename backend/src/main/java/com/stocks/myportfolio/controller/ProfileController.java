package com.stocks.myportfolio.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.dto.response.auth.UserResponse;
import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.repository.UserRepository;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public UserResponse getProfile(Principal principal) {
        User user = findUser(principal.getName());
        return toResponse(user);
    }

    @PutMapping
    public UserResponse updateProfile(Principal principal, @RequestBody Map<String, String> body) {
        User user = findUser(principal.getName());
        if (body.containsKey("firstName")) user.setFirstName(body.get("firstName"));
        if (body.containsKey("lastName")) user.setLastName(body.get("lastName"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        userRepository.save(user);
        return toResponse(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getPhone(), user.getRole(), user.getStatus(),
                user.isEmailVerified());
    }
}
