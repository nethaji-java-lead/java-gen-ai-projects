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
public class ProcurementDTO {
    private Long id;
    private String documentRef;
    private LocalDate businessDate;
    private String senderId;
    private String receiverId;
    private BigDecimal quantity;
    private String unitOfMeasure;
    private String status;
}