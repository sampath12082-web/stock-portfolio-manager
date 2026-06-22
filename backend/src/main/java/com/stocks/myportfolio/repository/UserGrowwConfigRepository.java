package com.stocks.myportfolio.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.stocks.myportfolio.entity.UserGrowwConfig;
import com.stocks.myportfolio.entity.User;

public interface UserGrowwConfigRepository extends JpaRepository<UserGrowwConfig, Long> {

    Optional<UserGrowwConfig> findByUser(User user);

    Optional<UserGrowwConfig> findByUserId(Long userId);
}
