package com.cinema.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SeatInfo(
        @JsonProperty("seat_id") String seatId,
        @JsonProperty("user_id") String userId,
        boolean booked,
        boolean confirmed
) {}
