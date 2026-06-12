package com.cinema.web.dto;

import java.math.BigDecimal;

public record RevenueByMovieResponse(
        String movieTitle,
        BigDecimal revenue
) {
}
