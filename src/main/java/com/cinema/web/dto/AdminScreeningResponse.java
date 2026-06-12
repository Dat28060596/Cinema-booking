package com.cinema.web.dto;

public record AdminScreeningResponse(
        Long id,
        String movieId,
        String movieTitle,
        String screeningDate,
        String dateLabel,
        String screeningTime,
        String hall,
        String format,
        int seatsTotal,
        int seatsHeld,
        int seatsBooked
) {
}
