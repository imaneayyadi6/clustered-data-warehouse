package com.example.cdw.service;

import com.example.cdw.model.Deal;
import com.example.cdw.repository.DealRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DealService {

    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public List<Map<String, String>> importDeals(InputStream inputStream) {
        List<Map<String, String>> results = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String header = br.readLine(); // skip header

            String line;
            int lineNumber = 2;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");

                Map<String, String> result = new HashMap<>();
                result.put("line", String.valueOf(lineNumber));

                if (parts.length != 5) {
                    result.put("status", "error");
                    result.put("message", "invalid row format");
                    results.add(result);
                    lineNumber++;
                    continue;
                }

                String dealUniqueId = parts[0];
                String fromIso = parts[1];
                String toIso = parts[2];
                String timestampStr = parts[3];
                String amountStr = parts[4];

                try {
                    Deal deal = new Deal();
                    deal.setDealUniqueId(dealUniqueId);
                    deal.setFromCurrencyIsoCode(fromIso);
                    deal.setToCurrencyIsoCode(toIso);
                    deal.setDealTimestamp(LocalDateTime.parse(timestampStr));
                    deal.setDealAmount(new BigDecimal(amountStr));

                    if (dealRepository.findByDealUniqueId(dealUniqueId).isPresent()) {
                        result.put("status", "duplicate");
                    } else {
                        dealRepository.save(deal);
                        result.put("status", "inserted");
                    }

                } catch (Exception e) {
                    result.put("status", "error");
                    result.put("message", e.getMessage());
                }

                results.add(result);
                lineNumber++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import CSV", e);
        }

        return results;
    }
}
