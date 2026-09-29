package com.tscnet.dailyprocess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDTO {
    private Long id;
    private String offerCode;
    private LocalDate businessDate;
    private String bidderId;
    private BigDecimal offeredPrice;
    private BigDecimal capacityMw;
    private String status;
}