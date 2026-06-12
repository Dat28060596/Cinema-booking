package com.cinema.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HoldSeatRequest(@JsonProperty("user_id") String userId) {}
