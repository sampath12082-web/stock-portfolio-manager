package com.stocks.myportfolio.repository.mf;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.myportfolio.common.enums.MfTransactionType;
import com.stocks.myportfolio.entity.mf.MfTransaction;
import com.stocks.myportfolio.entity.mf.MutualFund;

@Repository
public interface MfTransactionRepository extends JpaRepository<MfTransaction, Long> {

    List<MfTransaction> findAllByOrderByTradeDateDesc();

    List<MfTransaction> findByMutualFundOrderByTradeDateDesc(MutualFund fund);

    List<MfTransaction> findByTransactionTypeOrderByTradeDateDesc(MfTransactionType type);
}
