package com.cinema.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MovieResponse(
        String id,
        String title,
        int rows,
        @JsonProperty("seats_per_row") int seatsPerRow,
        @JsonProperty("poster_url") String posterUrl,
        String genre,
        int duration,
        String rating,
        int year,
        String synopsis,
        List<Screening> screenings
) {
    public record Screening(
            String date,
            @JsonProperty("date_label") String dateLabel,
            String time,
            String hall,
            String format,
            @JsonProperty("screening_id") Long screeningId
    ) {}
}
