package com.stocks.myportfolio.service.mf;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.stocks.myportfolio.dto.response.mf.MfLookupResponse;
import com.stocks.myportfolio.entity.mf.MutualFund;
import com.stocks.myportfolio.repository.mf.MutualFundRepository;

@Service
public class MfNavService {

    private static final Logger log = LoggerFactory.getLogger(MfNavService.class);
    private static final String AMFI_NAV_URL = "https://www.amfiindia.com/spages/NAVAll.txt";
    private static final DateTimeFormatter NAV_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final MutualFundRepository mutualFundRepository;

    public MfNavService(MutualFundRepository mutualFundRepository) {
        this.mutualFundRepository = mutualFundRepository;
    }

    @Scheduled(cron = "0 0 21 * * MON-FRI")
    public void refreshNavs() {
        log.info("Refreshing mutual fund NAVs from AMFI");
        List<MutualFund> trackedFunds = mutualFundRepository.findAll();
        if (trackedFunds.isEmpty()) return;

        java.util.Map<String, MutualFund> schemeMap = new java.util.HashMap<>();
        for (MutualFund f : trackedFunds) {
            schemeMap.put(f.getSchemeCode(), f);
        }

        int updated = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(URI.create(AMFI_NAV_URL).toURL().openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 6) continue;

                String schemeCode = parts[0].trim();
                MutualFund fund = schemeMap.get(schemeCode);
                if (fund == null) continue;

                try {
                    BigDecimal nav = new BigDecimal(parts[4].trim());
                    LocalDate navDate = LocalDate.parse(parts[5].trim(), NAV_DATE_FORMAT);
                    fund.setCurrentNav(nav);
                    fund.setNavDate(navDate);
                    mutualFundRepository.save(fund);
                    updated++;
                } catch (Exception e) {
                    log.warn("Failed to parse NAV for scheme {}: {}", schemeCode, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch AMFI NAV data: {}", e.getMessage());
        }

        log.info("NAV refresh complete: {} funds updated", updated);
    }

    public java.util.List<MfLookupResponse> searchAmfi(String query) {
        java.util.List<MfLookupResponse> results = new java.util.ArrayList<>();
        String queryLower = query.toLowerCase();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(URI.create(AMFI_NAV_URL).toURL().openStream()))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 20) {
                String[] parts = line.split(";");
                if (parts.length < 6) continue;

                String schemeName = parts[3].trim();
                String schemeCode = parts[0].trim();

                if (schemeName.toLowerCase().contains(queryLower)
                        || schemeCode.equals(query.trim())) {
                    try {
                        BigDecimal nav = new BigDecimal(parts[4].trim());
                        boolean exists = mutualFundRepository.findBySchemeCode(schemeCode).isPresent();
                        results.add(new MfLookupResponse(
                                schemeCode, schemeName, nav, parts[5].trim(), exists));
                        count++;
                    } catch (NumberFormatException e) {
                        // skip non-numeric NAV lines (headers, category labels)
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to search AMFI: {}", e.getMessage());
        }

        return results;
    }
}
