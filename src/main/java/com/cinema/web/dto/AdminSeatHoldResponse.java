package com.cinema.web.dto;

public record AdminSeatHoldResponse(
        Long screeningId,
        String movieTitle,
        String screeningDate,
        String screeningTime,
        String seatId,
        String userId
) {}
