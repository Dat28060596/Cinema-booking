package com.cinema.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HoldResponse(
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("movie_id") String movieId,
        @JsonProperty("seat_id") String seatId,
        @JsonProperty("expires_at") String expiresAt
) {}
