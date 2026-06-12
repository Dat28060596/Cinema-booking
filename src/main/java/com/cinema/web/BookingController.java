package com.cinema.web;

import com.cinema.application.BookingService;
import com.cinema.domain.entity.Booking;
import com.cinema.domain.entity.Seat;
import com.cinema.web.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class BookingController {

    private final BookingService svc;

    public BookingController(BookingService svc) {
        this.svc = svc;
    }

    @PostMapping("/screenings/{screeningId}/seats/{seatId}/hold")
    public ResponseEntity<?> holdSeat(
            @PathVariable Long screeningId,
            @PathVariable String seatId,
            @RequestBody HoldSeatRequest req) {

        if (req == null || isBlank(req.userId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is required"));
        }

        svc.holdSeat(screeningId, seatId, req.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new HoldResponse(
                req.userId(),
                String.valueOf(screeningId),
                seatId,
                null
        ));
    }

    @GetMapping("/screenings/{screeningId}/seats")
    public List<SeatInfo> listSeats(@PathVariable Long screeningId) {
        List<Seat> seats = svc.listSeats(screeningId);
        List<SeatInfo> infos = new ArrayList<>();
        for (Seat s : seats) {
            boolean booked = s.getStatus() != Seat.SeatStatus.AVAILABLE;
            boolean confirmed = s.getStatus() == Seat.SeatStatus.BOOKED;
            infos.add(new SeatInfo(
                    s.getSeatId(),
                    s.getHeldBySessionId(),
                    booked,
                    confirmed
            ));
        }
        return infos;
    }

    @PostMapping("/screenings/{screeningId}/confirm")
    public ResponseEntity<?> confirmSession(
            @PathVariable Long screeningId,
            @RequestBody ConfirmBookingRequest req) {

        if (req == null || isBlank(req.userId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is required"));
        }
        if (req.seatIds() == null || req.seatIds().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Choose at least one seat"));
        }

        Booking booking = svc.confirmBooking(
                screeningId,
                req.userId(),
                req.email(),
                req.firstName(),
                req.lastName(),
                req.seatIds(),
                req.totalAmount(),
                req.ticketCounts()
        );

        return ResponseEntity.ok(new SessionResponse(
                String.valueOf(booking.getId()),
                String.valueOf(screeningId),
                req.seatIds().get(0),
                req.userId(),
                booking.getStatus().name(),
                null
        ));
    }

    @DeleteMapping("/screenings/{screeningId}/seats/{seatId}")
    public ResponseEntity<?> releaseSession(
            @PathVariable Long screeningId,
            @PathVariable String seatId,
            @RequestBody HoldSeatRequest req) {

        if (req == null || isBlank(req.userId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is required"));
        }

        svc.releaseSeat(screeningId, seatId, req.userId());

        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
