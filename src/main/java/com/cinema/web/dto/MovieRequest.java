package com.cinema.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MovieRequest(
        String id,
        String title,
        int rows,
        @JsonProperty("seats_per_row") int seatsPerRow,
        @JsonProperty("poster_url") String posterUrl,
        String genre,
        int duration,
        String rating,
        int year,
        String synopsis
) {
}
