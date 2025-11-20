package com.example.cdw.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "deals", uniqueConstraints = {
        @UniqueConstraint(columnNames = "dealUniqueId")
})
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dealUniqueId;

    @Column(nullable = false)
    private String fromCurrencyIsoCode;

    @Column(nullable = false)
    private String toCurrencyIsoCode;

    @Column(nullable = false)
    private LocalDateTime dealTimestamp;

    @Column(nullable = false)
    private BigDecimal dealAmount; // FIXED

    public Deal() {
    }

    public Deal(String dealUniqueId, String fromCurrencyIsoCode, String toCurrencyIsoCode,
            LocalDateTime dealTimestamp, BigDecimal dealAmount) { // FIXED
        this.dealUniqueId = dealUniqueId;
        this.fromCurrencyIsoCode = fromCurrencyIsoCode;
        this.toCurrencyIsoCode = toCurrencyIsoCode;
        this.dealTimestamp = dealTimestamp;
        this.dealAmount = dealAmount;
    }

    public Long getId() {
        return id;
    }

    public String getDealUniqueId() {
        return dealUniqueId;
    }

    public void setDealUniqueId(String dealUniqueId) {
        this.dealUniqueId = dealUniqueId;
    }

    public String getFromCurrencyIsoCode() {
        return fromCurrencyIsoCode;
    }

    public void setFromCurrencyIsoCode(String fromCurrencyIsoCode) {
        this.fromCurrencyIsoCode = fromCurrencyIsoCode;
    }

    public String getToCurrencyIsoCode() {
        return toCurrencyIsoCode;
    }

    public void setToCurrencyIsoCode(String toCurrencyIsoCode) {
        this.toCurrencyIsoCode = toCurrencyIsoCode;
    }

    public LocalDateTime getDealTimestamp() {
        return dealTimestamp;
    }

    public void setDealTimestamp(LocalDateTime dealTimestamp) {
        this.dealTimestamp = dealTimestamp;
    }

    public BigDecimal getDealAmount() { // FIXED
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) { // FIXED
        this.dealAmount = dealAmount;
    }
}
