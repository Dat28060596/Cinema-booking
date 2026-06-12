package com.cinema.web.dto;

import java.time.LocalDate;

public record ScreeningRequest(
        String movieId,
        LocalDate screeningDate,
        String dateLabel,
        String screeningTime,
        String hall,
        String format
) {
}
