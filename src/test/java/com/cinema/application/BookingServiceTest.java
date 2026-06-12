package com.cinema.application;

import com.cinema.domain.entity.Booking;
import com.cinema.domain.entity.Movie;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.Seat;
import com.cinema.domain.entity.Ticket;
import com.cinema.infrastructure.persistence.SeatAlreadyBookedException;
import com.cinema.infrastructure.persistence.repository.BookingRepository;
import com.cinema.infrastructure.persistence.repository.MovieRepository;
import com.cinema.infrastructure.persistence.repository.ScreeningRepository;
import com.cinema.infrastructure.persistence.repository.SeatRepository;
import com.cinema.infrastructure.persistence.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking_service_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MovieRepository movieRepo;

    @Autowired
    private ScreeningRepository screeningRepo;

    @Autowired
    private SeatRepository seatRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private TicketRepository ticketRepo;

    @Test
    void confirmBookingBooksHeldSeats() {
        Screening screening = createScreening();
        createSeat(screening, "A", 1);
        createSeat(screening, "A", 2);

        bookingService.holdSeat(screening.getId(), "A1", "user1");
        bookingService.holdSeat(screening.getId(), "A2", "user1");

        Booking booking = bookingService.confirmBooking(
                screening.getId(),
                "user1",
                "test@example.com",
                "Test",
                "User",
                List.of("A1", "A2"),
                new BigDecimal("20.00")
        );

        Seat seatOne = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "A1");
        Seat seatTwo = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "A2");
        long ticketCount = countTicketsForBooking(booking.getId());

        assertNotNull(booking.getId());
        assertEquals(Booking.BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(Seat.SeatStatus.BOOKED, seatOne.getStatus());
        assertEquals(Seat.SeatStatus.BOOKED, seatTwo.getStatus());
        assertNull(seatOne.getHeldBySessionId());
        assertNull(seatTwo.getHeldBySessionId());
        assertEquals(2, ticketCount);
    }

    @Test
    void confirmBookingRejectsSeatHeldByAnotherUser() {
        Screening screening = createScreening();
        createSeat(screening, "B", 1);

        bookingService.holdSeat(screening.getId(), "B1", "user1");
        long bookingsBefore = bookingRepo.count();
        long ticketsBefore = ticketRepo.count();

        assertThrows(SeatAlreadyBookedException.class, () -> bookingService.confirmBooking(
                screening.getId(),
                "user2",
                "test@example.com",
                "Test",
                "User",
                List.of("B1"),
                new BigDecimal("10.00")
        ));

        Seat seat = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "B1");

        assertEquals(Seat.SeatStatus.HELD, seat.getStatus());
        assertEquals("user1", seat.getHeldBySessionId());
        assertEquals(bookingsBefore, bookingRepo.count());
        assertEquals(ticketsBefore, ticketRepo.count());
    }

    @Test
    void confirmBookingRejectsSeatThatWasNotHeld() {
        Screening screening = createScreening();
        createSeat(screening, "C", 1);
        long bookingsBefore = bookingRepo.count();

        assertThrows(SeatAlreadyBookedException.class, () -> bookingService.confirmBooking(
                screening.getId(),
                "user1",
                "test@example.com",
                "Test",
                "User",
                List.of("C1"),
                new BigDecimal("10.00")
        ));

        Seat seat = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "C1");

        assertEquals(Seat.SeatStatus.AVAILABLE, seat.getStatus());
        assertEquals(bookingsBefore, bookingRepo.count());
    }

    @Test
    void confirmBookingRejectsDuplicateSeats() {
        Screening screening = createScreening();
        createSeat(screening, "F", 1);

        bookingService.holdSeat(screening.getId(), "F1", "user1");
        long bookingsBefore = bookingRepo.count();

        assertThrows(IllegalArgumentException.class, () -> bookingService.confirmBooking(
                screening.getId(),
                "user1",
                "duplicate@example.com",
                "Duplicate",
                "User",
                List.of("F1", "F1"),
                new BigDecimal("20.00")
        ));

        Seat seat = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "F1");

        assertEquals(Seat.SeatStatus.HELD, seat.getStatus());
        assertEquals(bookingsBefore, bookingRepo.count());
    }

    @Test
    void confirmBookingPersistsSelectedTicketTypes() {
        Screening screening = createScreening();
        createSeat(screening, "G", 1);
        createSeat(screening, "G", 2);

        bookingService.holdSeat(screening.getId(), "G1", "user1");
        bookingService.holdSeat(screening.getId(), "G2", "user1");

        Booking booking = bookingService.confirmBooking(
                screening.getId(),
                "user1",
                "types@example.com",
                "Ticket",
                "Types",
                List.of("G1", "G2"),
                new BigDecimal("23.00"),
                Map.of("normal", 1, "students", 1)
        );

        List<Ticket> tickets = ticketRepo.findByBookingId(booking.getId());
        Ticket normalTicket = findTicketByType(tickets, "normal");
        Ticket studentTicket = findTicketByType(tickets, "students");

        assertEquals(2, tickets.size());
        assertNotNull(normalTicket);
        assertEquals(new BigDecimal("13.00"), normalTicket.getPrice());
        assertNotNull(studentTicket);
        assertEquals(new BigDecimal("10.00"), studentTicket.getPrice());
    }

    @Test
    void confirmBookingRejectsTotalThatDoesNotMatchTicketPrices() {
        Screening screening = createScreening();
        createSeat(screening, "H", 1);
        createSeat(screening, "H", 2);

        bookingService.holdSeat(screening.getId(), "H1", "user1");
        bookingService.holdSeat(screening.getId(), "H2", "user1");

        assertThrows(IllegalArgumentException.class, () -> bookingService.confirmBooking(
                screening.getId(),
                "user1",
                "wrongtotal@example.com",
                "Wrong",
                "Total",
                List.of("H1", "H2"),
                new BigDecimal("30.00"),
                Map.of("normal", 1, "students", 1)
        ));

        assertEquals(Seat.SeatStatus.HELD, seatRepo.findByScreeningIdAndSeatId(screening.getId(), "H1").getStatus());
        assertEquals(Seat.SeatStatus.HELD, seatRepo.findByScreeningIdAndSeatId(screening.getId(), "H2").getStatus());
    }

    @Test
    void deleteBookingCancelsAndReleasesSeats() {
        Screening screening = createScreening();
        createSeat(screening, "D", 1);
        createSeat(screening, "D", 2);

        bookingService.holdSeat(screening.getId(), "D1", "user1");
        bookingService.holdSeat(screening.getId(), "D2", "user1");

        Booking booking = bookingService.confirmBooking(
                screening.getId(),
                "user1",
                "delete@example.com",
                "Delete",
                "User",
                List.of("D1", "D2"),
                new BigDecimal("26.00")
        );

        bookingService.deleteBooking(booking.getId());
        bookingRepo.flush();

        Booking cancelledBooking = bookingRepo.findById(booking.getId()).orElseThrow();
        Seat seatOne = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "D1");
        Seat seatTwo = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "D2");

        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());
        assertFalse(bookingService.listConfirmedBookings().contains(cancelledBooking));
        assertEquals(Seat.SeatStatus.AVAILABLE, seatOne.getStatus());
        assertEquals(Seat.SeatStatus.AVAILABLE, seatTwo.getStatus());
        assertNull(seatOne.getHeldBySessionId());
        assertNull(seatTwo.getHeldBySessionId());
    }

    @Test
    void releaseHeldSeatsClearsTemporaryHoldsOnly() {
        Screening screening = createScreening();
        createSeat(screening, "E", 1);
        createSeat(screening, "E", 2);

        bookingService.holdSeat(screening.getId(), "E1", "user1");
        bookingService.holdSeat(screening.getId(), "E2", "user2");
        bookingService.confirmBooking(
                screening.getId(),
                "user2",
                "held@example.com",
                "Held",
                "User",
                List.of("E2"),
                new BigDecimal("13.00")
        );

        int released = bookingService.releaseHeldSeats();
        Seat heldSeat = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "E1");
        Seat bookedSeat = seatRepo.findByScreeningIdAndSeatId(screening.getId(), "E2");

        assertEquals(1, released);
        assertEquals(Seat.SeatStatus.AVAILABLE, heldSeat.getStatus());
        assertNull(heldSeat.getHeldBySessionId());
        assertEquals(Seat.SeatStatus.BOOKED, bookedSeat.getStatus());
    }

    private Screening createScreening() {
        Movie movie = new Movie(
                "test-" + UUID.randomUUID(),
                "Test Movie",
                1,
                2,
                "/posters/test.png",
                "Drama",
                100,
                "Rated 12",
                2026,
                "Test synopsis"
        );
        movieRepo.save(movie);

        Screening screening = new Screening(LocalDate.now(), "Today", "18:00", "Hall Test", "2D");
        screening.setMovie(movie);
        return screeningRepo.save(screening);
    }

    private Seat createSeat(Screening screening, String row, int number) {
        String seatId = row + number;
        return seatRepo.save(new Seat(screening, row, number, seatId));
    }

    private long countTicketsForBooking(Long bookingId) {
        long count = 0;
        for (Ticket ticket : ticketRepo.findAll()) {
            if (ticket.getBooking().getId().equals(bookingId)) {
                count++;
            }
        }
        return count;
    }

    private Ticket findTicketByType(List<Ticket> tickets, String type) {
        for (Ticket ticket : tickets) {
            if (type.equals(ticket.getType())) {
                return ticket;
            }
        }
        return null;
    }
}
