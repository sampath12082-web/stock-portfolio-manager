package com.stocks.myportfolio.repository.mf;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.entity.mf.MfHolding;
import com.stocks.myportfolio.entity.mf.MutualFund;

@Repository
public interface MfHoldingRepository extends JpaRepository<MfHolding, Long> {

    Optional<MfHolding> findByMutualFund(MutualFund mutualFund);
    List<MfHolding> findByUserId(Long userId);
}
