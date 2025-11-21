package com.example.cdw.service;

import com.example.cdw.model.Deal;
import com.example.cdw.repository.DealRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private DealService dealService;

    // ------------------------------
    // Test 1 — Valid rows must be saved
    // ------------------------------
    @Test
    void testImport_validRows_saved() {

        String csv = """
                dealUniqueId,fromCurrencyIsoCode,toCurrencyIsoCode,dealTimestamp,dealAmount
                D1,USD,EUR,2023-11-20T10:00:00,1000
                D2,EUR,JPY,2023-11-20T12:00:00,2000
                """;

        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(dealRepository.findByDealUniqueId("D1")).thenReturn(Optional.empty());
        when(dealRepository.findByDealUniqueId("D2")).thenReturn(Optional.empty());

        List<Map<String, String>> result = dealService.importDeals(is);

        verify(dealRepository, times(2)).save(any(Deal.class));
        assertEquals("inserted", result.get(0).get("status"));
        assertEquals("inserted", result.get(1).get("status"));
    }

    // ------------------------------
    // Test 2 — Duplicate row must be skipped
    // ------------------------------
    @Test
    void testImport_duplicateRow_skipped() {

        String csv = """
                dealUniqueId,fromCurrencyIsoCode,toCurrencyIsoCode,dealTimestamp,dealAmount
                D1,USD,EUR,2023-11-20T10:00:00,1000
                """;

        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        when(dealRepository.findByDealUniqueId("D1"))
                .thenReturn(Optional.of(new Deal()));  // Means: already exists

        List<Map<String, String>> result = dealService.importDeals(is);

        verify(dealRepository, never()).save(any(Deal.class));
        assertEquals("duplicate", result.get(0).get("status"));
    }

    // ------------------------------
    // Test 3 — Partial failure: first saves, second errors
    // ------------------------------
    @Test
    void testImport_partialFailure_noRollback() {

        String csv = """
                dealUniqueId,fromCurrencyIsoCode,toCurrencyIsoCode,dealTimestamp,dealAmount
                D1,USD,EUR,2023-11-20T10:00:00,1000
                BAD,USD,EUR,WRONG_DATE,abc
                """;

        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        // ONLY stub required valid row
        when(dealRepository.findByDealUniqueId("D1")).thenReturn(Optional.empty());

        List<Map<String, String>> result = dealService.importDeals(is);

        // valid row saved once
        verify(dealRepository, times(1)).save(any(Deal.class));

        // first row OK
        assertEquals("inserted", result.get(0).get("status"));

        // second row error
        assertEquals("error", result.get(1).get("status"));
    }
}
