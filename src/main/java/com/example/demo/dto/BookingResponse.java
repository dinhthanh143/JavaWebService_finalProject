package com.example.demo.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String courtName;
    private String clusterName;
    private String timeSlot;
    private LocalDate bookingDate;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
}