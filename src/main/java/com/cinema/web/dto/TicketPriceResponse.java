package com.cinema.web.dto;

import java.math.BigDecimal;

public record TicketPriceResponse(
        String code,
        String label,
        BigDecimal amount
) {
}
