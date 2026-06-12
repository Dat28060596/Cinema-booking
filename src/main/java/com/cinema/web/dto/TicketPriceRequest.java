package com.cinema.web.dto;

import java.math.BigDecimal;

public record TicketPriceRequest(
        BigDecimal amount
) {
}
