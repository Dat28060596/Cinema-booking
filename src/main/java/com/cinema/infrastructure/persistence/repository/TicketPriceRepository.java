package com.cinema.infrastructure.persistence.repository;

import com.cinema.domain.entity.TicketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPriceRepository extends JpaRepository<TicketPrice, String> {
    List<TicketPrice> findAllByOrderByDisplayOrderAsc();
}
