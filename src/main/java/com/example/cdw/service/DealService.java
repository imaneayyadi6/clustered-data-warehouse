package com.example.cdw.service;

import com.example.cdw.model.Deal;
import com.example.cdw.repository.DealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DealService {

    private static final Logger logger = LoggerFactory.getLogger(DealService.class);

    private final DealRepository dealRepository;

    // validation patterns
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    /**
     * Import deals from CSV input stream.
     * Return per-row results: { line, dealUniqueId, status: saved|skipped|error, message? }
     */
    public List<Map<String, String>> importDeals(InputStream inputStream) {
        List<Map<String, String>> results = new ArrayList<>();
        logger.info("Import started");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String header = br.readLine(); // header line (may be null)
            if (header == null) {
                logger.warn("Uploaded file is empty");
                return results;
            }

            String line;
            int lineNumber = 2; // header counted as line 1

            while ((line = br.readLine()) != null) {
                Map<String, String> rowResult = new LinkedHashMap<>();
                rowResult.put("line", String.valueOf(lineNumber));

                // split CSV, allow empty values but keep their positions
                String[] parts = line.split(",", -1);

                String dealUniqueId = "";
                try {
                    // basic field count
                    if (parts.length != 5) {
                        rowResult.put("status", "error");
                        rowResult.put("message", "invalid row format (expected 5 columns)");
                        results.add(rowResult);
                        logger.warn("Line {} invalid row format", lineNumber);
                        lineNumber++;
                        continue;
                    }

                    dealUniqueId = parts[0].trim();
                    String fromIso = parts[1].trim();
                    String toIso = parts[2].trim();
                    String timestampStr = parts[3].trim();
                    String amountStr = parts[4].trim();

                    rowResult.put("dealUniqueId", dealUniqueId);

                    // validation
                    if (dealUniqueId.isEmpty()) {
                        rowResult.put("status", "error");
                        rowResult.put("message", "dealUniqueId missing");
                        results.add(rowResult);
                        logger.warn("Line {} missing dealUniqueId", lineNumber);
                        lineNumber++;
                        continue;
                    }

                    if (!CURRENCY_PATTERN.matcher(fromIso).matches()) {
                        rowResult.put("status", "error");
                        rowResult.put("message", "fromCurrencyIsoCode invalid (must be 3 uppercase letters)");
                        results.add(rowResult);
                        logger.warn("Line {} invalid fromCurrencyIsoCode: {}", lineNumber, fromIso);
                        lineNumber++;
                        continue;
                    }

                    if (!CURRENCY_PATTERN.matcher(toIso).matches()) {
                        rowResult.put("status", "error");
                        rowResult.put("message", "toCurrencyIsoCode invalid (must be 3 uppercase letters)");
                        results.add(rowResult);
                        logger.warn("Line {} invalid toCurrencyIsoCode: {}", lineNumber, toIso);
                        lineNumber++;
                        continue;
                    }

                    LocalDateTime parsedTimestamp;
                    try {
                        parsedTimestamp = parseTimestamp(timestampStr);
                    } catch (Exception e) {
                        rowResult.put("status", "error");
                        rowResult.put("message", "invalid timestamp format: " + e.getMessage());
                        results.add(rowResult);
                        logger.warn("Line {} invalid timestamp: {}", lineNumber, timestampStr);
                        lineNumber++;
                        continue;
                    }

                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(amountStr);
                    } catch (Exception e) {
                        rowResult.put("status", "error");
                        rowResult.put("message", "invalid amount: " + amountStr);
                        results.add(rowResult);
                        logger.warn("Line {} invalid amount: {}", lineNumber, amountStr);
                        lineNumber++;
                        continue;
                    }

                    // Duplicate check (optimistic)
if (dealRepository.findByDealUniqueId(dealUniqueId).isPresent()) {
    rowResult.put("status", "duplicate");
    results.add(rowResult);
    logger.info("Line {} duplicate {}", lineNumber, dealUniqueId);
    lineNumber++;
    continue;
}

                    // create entity and save (each row saved immediately)
                    Deal deal = new Deal();
                    deal.setDealUniqueId(dealUniqueId);
                    deal.setFromCurrencyIsoCode(fromIso);
                    deal.setToCurrencyIsoCode(toIso);
                    deal.setDealTimestamp(parsedTimestamp);
                    deal.setDealAmount(amount);

                    try {
                        dealRepository.save(deal);
                        rowResult.put("status", "inserted");
                        results.add(rowResult);
                        logger.info("Line {} saved deal {}", lineNumber, dealUniqueId);
                    } catch (DataIntegrityViolationException dive) {
                        // possible race condition: unique constraint failed
                        rowResult.put("status", "duplicate");
                        results.add(rowResult);
                        logger.warn("Line {} could not save due to unique constraint for {}", lineNumber, dealUniqueId);
                    } catch (Exception e) {
                        rowResult.put("status", "error");
                        rowResult.put("message", "save failed: " + e.getMessage());
                        results.add(rowResult);
                        logger.error("Line {} save failed for {}: {}", lineNumber, dealUniqueId, e.getMessage());
                    }

                } catch (Exception outer) {
                    // catch-all per-row to respect "no rollback"
                    rowResult.putIfAbsent("dealUniqueId", dealUniqueId);
                    rowResult.put("status", "error");
                    rowResult.put("message", "unexpected error: " + outer.getMessage());
                    results.add(rowResult);
                    logger.error("Line {} unexpected error: {}", lineNumber, outer.getMessage(), outer);
                }

                lineNumber++;
            }

        } catch (Exception e) {
            logger.error("Failed to read uploaded CSV", e);
            throw new RuntimeException("Failed to import CSV", e);
        }

        logger.info("Import finished, {} rows processed", results.size());
        return results;
    }

    // Try parse ISO_LOCAL_DATE_TIME first, then try OffsetDateTime -> toLocalDateTime
    private LocalDateTime parseTimestamp(String ts) {
        if (ts == null || ts.isEmpty()) {
            throw new IllegalArgumentException("timestamp empty");
        }
        try {
            // try LocalDateTime first
            return LocalDateTime.parse(ts, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e1) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(ts, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                return odt.toLocalDateTime();
            } catch (Exception e2) {
                // sometimes with 'Z' as zone
                try {
                    Instant instant = Instant.parse(ts);
                    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                } catch (Exception e3) {
                    throw new IllegalArgumentException("unrecognized timestamp format");
                }
            }
        }
    }
}
