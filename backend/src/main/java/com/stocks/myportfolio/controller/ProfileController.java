package com.stocks.myportfolio.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;

import com.stocks.myportfolio.common.exception.ResourceNotFoundException;
import com.stocks.myportfolio.dto.response.auth.UserResponse;
import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.entity.UserGrowwConfig;
import com.stocks.myportfolio.integration.groww.GrowwClient;
import com.stocks.myportfolio.repository.UserGrowwConfigRepository;
import com.stocks.myportfolio.repository.UserRepository;
import com.stocks.myportfolio.service.RsaKeyService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserGrowwConfigRepository growwConfigRepository;
    private final RsaKeyService rsaKeyService;
    private final GrowwClient growwClient;

    public ProfileController(UserRepository userRepository, UserGrowwConfigRepository growwConfigRepository,
            RsaKeyService rsaKeyService, Optional<GrowwClient> growwClient) {
        this.userRepository = userRepository;
        this.growwConfigRepository = growwConfigRepository;
        this.rsaKeyService = rsaKeyService;
        this.growwClient = growwClient.orElse(null);
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProfileController.class);

    @GetMapping("/groww")
    public Map<String, Object> getGrowwConfig(Principal principal) {
        User user = findUser(principal.getName());
        return growwConfigRepository.findByUser(user)
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    boolean hasToken = c.getAccessTokenEncrypted() != null && !c.getAccessTokenEncrypted().isBlank();
                    boolean hasSecret = c.getApiSecretEncrypted() != null && !c.getApiSecretEncrypted().isBlank();
                    m.put("hasAccessToken", hasToken);
                    m.put("hasApiSecret", hasSecret);

                    if (hasToken && hasSecret && growwClient != null) {
                        Map<String, Object> validation = growwClient.validateCredentials(
                                c.getAccessTokenEncrypted(), c.getApiSecretEncrypted());
                        boolean connected = Boolean.TRUE.equals(validation.get("valid"));
                        m.put("connected", connected);
                        m.put("enabled", connected);
                        m.put("validationMessage", validation.get("message"));
                        if (!connected) {
                            log.warn("Groww connection failed for user {}: {}", user.getEmail(), validation.get("message"));
                            c.setEnabled(false);
                            growwConfigRepository.save(c);
                        } else if (!c.isEnabled()) {
                            c.setEnabled(true);
                            growwConfigRepository.save(c);
                        }
                    } else {
                        m.put("connected", false);
                        m.put("enabled", c.isEnabled());
                    }

                    if (hasToken) {
                        String t = c.getAccessTokenEncrypted();
                        m.put("accessTokenPreview", t.substring(0, Math.min(6, t.length())) + "..." + t.substring(Math.max(0, t.length() - 4)));
                        m.put("accessTokenLength", t.length());
                    }
                    if (hasSecret) {
                        String s = c.getApiSecretEncrypted();
                        m.put("apiSecretPreview", s.substring(0, Math.min(6, s.length())) + "..." + s.substring(Math.max(0, s.length() - 4)));
                        m.put("apiSecretLength", s.length());
                    }
                    return m;
                })
                .orElse(Map.of("enabled", false, "hasAccessToken", false, "hasApiSecret", false, "connected", false));
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Groww config saved");
        result.put("enabled", config.isEnabled());

        if (hasCreds && growwClient != null) {
            Map<String, Object> validation = growwClient.validateCredentials(
                    config.getAccessTokenEncrypted(), config.getApiSecretEncrypted());
            boolean valid = Boolean.TRUE.equals(validation.get("valid"));
            config.setEnabled(valid);
            growwConfigRepository.save(config);
            result.put("enabled", valid);
            result.put("connected", valid);
            result.put("validationMessage", validation.get("message"));
        }

        return result;
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
