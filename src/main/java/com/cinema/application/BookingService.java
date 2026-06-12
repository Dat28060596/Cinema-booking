package com.cinema.application;

import com.cinema.domain.entity.*;
import com.cinema.infrastructure.persistence.SeatAlreadyBookedException;
import com.cinema.infrastructure.persistence.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final TicketRepository ticketRepo;
    private final SeatRepository seatRepo;
    private final CustomerRepository customerRepo;
    private final ScreeningRepository screeningRepo;
    private final TicketPriceRepository ticketPriceRepo;

    public BookingService(BookingRepository bookingRepo, TicketRepository ticketRepo, SeatRepository seatRepo, CustomerRepository customerRepo, ScreeningRepository screeningRepo, TicketPriceRepository ticketPriceRepo) {
        this.bookingRepo = bookingRepo;
        this.ticketRepo = ticketRepo;
        this.seatRepo = seatRepo;
        this.customerRepo = customerRepo;
        this.screeningRepo = screeningRepo;
        this.ticketPriceRepo = ticketPriceRepo;
    }

    @Transactional
    public Seat holdSeat(Long screeningId, String seatId, String userId) {
        if (isBlank(userId)) {
            throw new IllegalArgumentException("User is required");
        }

        Seat seat = seatRepo.findByScreeningIdAndSeatId(screeningId, seatId);
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found");
        }

        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new SeatAlreadyBookedException("Seat already held or booked");
        }

        seat.setStatus(Seat.SeatStatus.HELD);
        seat.setHeldBySessionId(userId);
        return seatRepo.save(seat);
    }

    @Transactional
    public Booking confirmBooking(Long screeningId, String userId, String email, String firstName, String lastName, List<String> seatIds, BigDecimal totalAmount) {
        return confirmBooking(screeningId, userId, email, firstName, lastName, seatIds, totalAmount, null);
    }

    @Transactional
    public Booking confirmBooking(Long screeningId, String userId, String email, String firstName, String lastName, List<String> seatIds, BigDecimal totalAmount, Map<String, Integer> ticketCounts) {
        validateBooking(userId, email, firstName, lastName, seatIds, totalAmount);

        Screening screening = screeningRepo.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));

        List<Seat> seats = findHeldSeats(screeningId, userId, seatIds);
        List<TicketLine> ticketLines = buildTicketLines(ticketCounts, seats.size(), totalAmount);
        Customer customer = findOrCreateCustomer(email, firstName, lastName);
        Booking booking = new Booking(customer, screening, totalAmount);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepo.save(booking);

        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            TicketLine ticketLine = ticketLines.get(i);
            seat.setStatus(Seat.SeatStatus.BOOKED);
            seat.setHeldBySessionId(null);
            seatRepo.save(seat);

            Ticket ticket = new Ticket(seat, ticketLine.type(), ticketLine.price());
            ticket.setBooking(booking);
            ticketRepo.save(ticket);
        }

        return booking;
    }

    private List<TicketLine> buildTicketLines(Map<String, Integer> ticketCounts, int seatCount, BigDecimal totalAmount) {
        List<TicketLine> ticketLines = new ArrayList<>();

        if (ticketCounts == null || ticketCounts.isEmpty()) {
            BigDecimal ticketPrice = totalAmount.divide(BigDecimal.valueOf(seatCount), 2, RoundingMode.HALF_UP);
            for (int i = 0; i < seatCount; i++) {
                ticketLines.add(new TicketLine("normal", ticketPrice));
            }
            return ticketLines;
        }

        List<String> knownCodes = new ArrayList<>();
        BigDecimal expectedTotal = BigDecimal.ZERO;
        int selectedTickets = 0;

        for (TicketPrice ticketPrice : ticketPriceRepo.findAllByOrderByDisplayOrderAsc()) {
            knownCodes.add(ticketPrice.getCode());
            Integer requestedCount = ticketCounts.get(ticketPrice.getCode());
            int count = requestedCount == null ? 0 : requestedCount;
            if (count < 0) {
                throw new IllegalArgumentException("Ticket count cannot be negative");
            }
            selectedTickets += count;
            for (int i = 0; i < count; i++) {
                ticketLines.add(new TicketLine(ticketPrice.getCode(), ticketPrice.getAmount()));
                expectedTotal = expectedTotal.add(ticketPrice.getAmount());
            }
        }

        for (Map.Entry<String, Integer> entry : ticketCounts.entrySet()) {
            if (!knownCodes.contains(entry.getKey()) && entry.getValue() != null && entry.getValue() > 0) {
                throw new IllegalArgumentException("Unknown ticket type: " + entry.getKey());
            }
        }

        if (selectedTickets != seatCount) {
            throw new IllegalArgumentException("Choose one ticket type for each selected seat");
        }
        if (expectedTotal.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException("Total amount does not match ticket prices");
        }

        return ticketLines;
    }

    @Transactional
    public void releaseSeat(Long screeningId, String seatId, String userId) {
        Seat seat = seatRepo.findByScreeningIdAndSeatId(screeningId, seatId);
        if (seat != null && Seat.SeatStatus.HELD.equals(seat.getStatus()) && userId.equals(seat.getHeldBySessionId())) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldBySessionId(null);
            seatRepo.save(seat);
        }
    }

    public List<Seat> listSeats(Long screeningId) {
        return seatRepo.findByScreeningId(screeningId);
    }

    public List<Booking> listConfirmedBookings() {
        List<Booking> confirmedBookings = new ArrayList<>();
        for (Booking booking : bookingRepo.findAll()) {
            if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                confirmedBookings.add(booking);
            }
        }
        return confirmedBookings;
    }

    public List<Seat> listHeldSeats() {
        return seatRepo.findByStatus(Seat.SeatStatus.HELD);
    }

    @Transactional
    public int releaseHeldSeats() {
        List<Seat> seats = listHeldSeats();
        for (Seat seat : seats) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldBySessionId(null);
            seatRepo.save(seat);
        }
        return seats.size();
    }

    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        for (Ticket ticket : ticketRepo.findByBookingId(bookingId)) {
            Seat seat = ticket.getSeat();
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setHeldBySessionId(null);
            seatRepo.save(seat);
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepo.save(booking);
    }

    private void validateBooking(String userId, String email, String firstName, String lastName, List<String> seatIds, BigDecimal totalAmount) {
        if (isBlank(userId)) {
            throw new IllegalArgumentException("User is required");
        }
        if (isBlank(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        if (isBlank(firstName)) {
            throw new IllegalArgumentException("First name is required");
        }
        if (isBlank(lastName)) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one seat");
        }
        List<String> checkedSeatIds = new ArrayList<>();
        for (String seatId : seatIds) {
            if (isBlank(seatId)) {
                throw new IllegalArgumentException("Seat is required");
            }
            if (checkedSeatIds.contains(seatId)) {
                throw new IllegalArgumentException("Seat cannot be selected twice: " + seatId);
            }
            checkedSeatIds.add(seatId);
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero");
        }
    }

    private List<Seat> findHeldSeats(Long screeningId, String userId, List<String> seatIds) {
        List<Seat> seats = new ArrayList<>();
        for (String seatId : seatIds) {
            Seat seat = seatRepo.findByScreeningIdAndSeatId(screeningId, seatId);
            if (seat == null) {
                throw new IllegalArgumentException("Seat not found: " + seatId);
            }
            if (seat.getStatus() != Seat.SeatStatus.HELD || !userId.equals(seat.getHeldBySessionId())) {
                throw new SeatAlreadyBookedException("Seat is not held by this user: " + seatId);
            }
            seats.add(seat);
        }
        return seats;
    }

    private Customer findOrCreateCustomer(String email, String firstName, String lastName) {
        Customer customer = customerRepo.findByEmail(email);
        if (customer != null) {
            return customer;
        }
        return customerRepo.save(new Customer(email, "", firstName, lastName, false, "guest", false));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record TicketLine(String type, BigDecimal price) {
    }
}
