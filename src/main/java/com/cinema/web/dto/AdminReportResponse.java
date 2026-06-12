package com.cinema.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminReportResponse(
        BigDecimal totalRevenue,
        long confirmedBookings,
        long heldSeats,
        List<RevenueByMovieResponse> revenueByMovie
) {
}
