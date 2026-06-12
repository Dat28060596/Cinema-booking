package com.cinema.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionResponse(
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("movie_id") String movieId,
        @JsonProperty("seat_id") String seatId,
        @JsonProperty("user_id") String userId,
        String status,
        @JsonProperty("expires_at") String expiresAt
) {}
