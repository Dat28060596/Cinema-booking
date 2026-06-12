package com.cinema.web.dto;

public record AuthResponse(boolean success, String message, String userId, String username, String role) {
}
