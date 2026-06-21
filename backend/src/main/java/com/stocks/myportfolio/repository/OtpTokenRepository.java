package com.stocks.myportfolio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocks.myportfolio.entity.OtpToken;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String email, String purpose);
}
