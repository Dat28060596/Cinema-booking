package com.cinema.infrastructure.persistence.repository;

import com.cinema.domain.entity.Booking;
import com.cinema.domain.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Advanced SQL Query 1: Find all confirmed bookings for a specific customer
    @Query("SELECT b FROM Booking b JOIN FETCH b.customer c WHERE c.email = :email AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedBookingsByCustomerEmail(@Param("email") String email);

    // Advanced SQL Query 2: Find all tickets booked for a specific movie screening, joining 4 tables
    @Query("SELECT t FROM Ticket t JOIN t.booking b JOIN b.screening s JOIN s.movie m WHERE m.id = :movieId AND b.status = 'CONFIRMED'")
    List<Ticket> findConfirmedTicketsByMovieId(@Param("movieId") String movieId);
    
    // Advanced SQL Query 3: Find revenue by movie
    @Query("SELECT m.title, SUM(b.totalAmount) FROM Booking b JOIN b.screening s JOIN s.movie m WHERE b.status = 'CONFIRMED' GROUP BY m.id, m.title")
    List<Object[]> findRevenueByMovie();
}
