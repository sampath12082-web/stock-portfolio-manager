package com.stocks.myportfolio.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.stocks.myportfolio.entity.Faq;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByActiveTrueOrderBySortOrderAsc();
}
