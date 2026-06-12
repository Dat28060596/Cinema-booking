package com.cinema.infrastructure.persistence.repository;

import com.cinema.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreeningId(Long screeningId);
    Seat findByScreeningIdAndSeatId(Long screeningId, String seatId);
    List<Seat> findByStatus(Seat.SeatStatus status);
}
