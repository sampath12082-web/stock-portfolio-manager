package com.stocks.myportfolio.service;

import com.stocks.myportfolio.entity.User;
import com.stocks.myportfolio.repository.UserRepository;
import com.stocks.myportfolio.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTest {

    @Mock private UserRepository userRepository;

    @Test
    void returnsUserId_whenAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null));

        CurrentUserProvider provider = new CurrentUserProvider(userRepository);
        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));
        assertDoesNotThrow(provider::getUserId);

        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsNull_whenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        CurrentUserProvider provider = new CurrentUserProvider(userRepository);
        assertNull(provider.getUserId());
    }

    @Test
    void returnsNull_whenAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null));
        CurrentUserProvider provider = new CurrentUserProvider(userRepository);
        assertNull(provider.getUserId());
        SecurityContextHolder.clearContext();
    }
}
