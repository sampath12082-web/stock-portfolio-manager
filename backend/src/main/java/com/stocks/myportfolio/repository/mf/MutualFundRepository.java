package com.stocks.myportfolio.repository.mf;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.entity.mf.MutualFund;

@Repository
public interface MutualFundRepository extends JpaRepository<MutualFund, Long> {

    Optional<MutualFund> findBySchemeCode(String schemeCode);

    List<MutualFund> findBySchemeNameContainingIgnoreCase(String query);

    List<MutualFund> findByFundHouseIgnoreCase(String fundHouse);
    List<MutualFund> findByUserId(Long userId);
}
