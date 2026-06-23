package com.stocks.myportfolio.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.dto.response.auth.UserResponse;
import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.entity.UserGrowwConfig;
import com.stocks.myportfolio.repository.UserGrowwConfigRepository;
import com.stocks.myportfolio.repository.UserRepository;
import com.stocks.myportfolio.service.RsaKeyService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserGrowwConfigRepository growwConfigRepository;
    private final RsaKeyService rsaKeyService;

    public ProfileController(UserRepository userRepository, UserGrowwConfigRepository growwConfigRepository,
            RsaKeyService rsaKeyService) {
        this.userRepository = userRepository;
        this.growwConfigRepository = growwConfigRepository;
        this.rsaKeyService = rsaKeyService;
    }

    private String decryptIfNeeded(String value) {
        if (value == null || value.isBlank()) return value;
        try {
            return rsaKeyService.decrypt(value);
        } catch (Exception e) {
            if (value.length() > 100 && value.matches("[A-Za-z0-9+/=]+")) {
                throw new com.stocks.myportfolio.common.exception.ValidationException(
                        "Credential decryption failed. Please refresh the page and try again.");
            }
            return value;
        }
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

    @GetMapping("/groww")
    public Map<String, Object> getGrowwConfig(Principal principal) {
        User user = findUser(principal.getName());
        return growwConfigRepository.findByUser(user)
                .map(c -> Map.<String, Object>of(
                        "enabled", c.isEnabled(),
                        "hasAccessToken", c.getAccessTokenEncrypted() != null,
                        "hasApiSecret", c.getApiSecretEncrypted() != null))
                .orElse(Map.of("enabled", false, "hasAccessToken", false, "hasApiSecret", false));
    }

    @PutMapping("/groww")
    public Map<String, Object> updateGrowwConfig(Principal principal, @RequestBody Map<String, String> body) {
        User user = findUser(principal.getName());
        UserGrowwConfig config = growwConfigRepository.findByUser(user).orElseGet(() -> {
            UserGrowwConfig c = new UserGrowwConfig();
            c.setUser(user);
            return c;
        });
        if (body.containsKey("accessToken")) config.setAccessTokenEncrypted(decryptIfNeeded(body.get("accessToken")));
        if (body.containsKey("apiSecret")) config.setApiSecretEncrypted(decryptIfNeeded(body.get("apiSecret")));
        if (body.containsKey("enabled")) config.setEnabled(Boolean.parseBoolean(body.get("enabled")));
        boolean hasCreds = config.getAccessTokenEncrypted() != null && !config.getAccessTokenEncrypted().isBlank()
                && config.getApiSecretEncrypted() != null && !config.getApiSecretEncrypted().isBlank();
        config.setEnabled(hasCreds);
        growwConfigRepository.save(config);
        return Map.of("message", "Groww config updated", "enabled", config.isEnabled());
    }

    @DeleteMapping("/groww")
    public Map<String, String> deleteGrowwConfig(Principal principal) {
        User user = findUser(principal.getName());
        growwConfigRepository.findByUser(user).ifPresent(growwConfigRepository::delete);
        return Map.of("message", "Groww config removed");
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
