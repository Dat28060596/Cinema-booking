package com.cinema.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminBookingResponse(
        Long id,
        String customerName,
        String customerEmail,
        String movieTitle,
        String screeningDate,
        String screeningTime,
        List<String> seatIds,
        BigDecimal totalAmount,
        String status
) {
}
