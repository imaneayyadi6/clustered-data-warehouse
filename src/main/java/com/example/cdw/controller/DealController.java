package com.example.cdw.controller;

import com.example.cdw.service.DealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.io.IOException;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping("/import")
    public ResponseEntity<List<Map<String, String>>> importDeals(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, String>> result = dealService.importDeals(file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
