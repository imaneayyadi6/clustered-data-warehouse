package com.example.cdw.controller;

import com.example.cdw.service.DealService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private static final Logger logger = LoggerFactory.getLogger(DealController.class);
    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping("/import")
    public ResponseEntity<List<Map<String, String>>> importDeals(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("Empty or missing file in upload");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of(Map.of("status", "error", "message", "invalid file")));
        }
        try (InputStream is = file.getInputStream()) {
            List<Map<String, String>> results = dealService.importDeals(is);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to read file upload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of(Map.of("status", "error", "message", "invalid file")));
        }
    }
}
