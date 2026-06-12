package com.cinema.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ConfirmBookingRequest(
    String userId,
    String email,
    String firstName,
    String lastName,
    List<String> seatIds,
    BigDecimal totalAmount,
    Map<String, Integer> ticketCounts
) {}
