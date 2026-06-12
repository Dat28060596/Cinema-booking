package com.cinema.web;

import com.cinema.domain.entity.TicketPrice;
import com.cinema.infrastructure.persistence.repository.TicketPriceRepository;
import com.cinema.web.dto.TicketPriceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TicketPriceController {

    private final TicketPriceRepository ticketPriceRepo;

    public TicketPriceController(TicketPriceRepository ticketPriceRepo) {
        this.ticketPriceRepo = ticketPriceRepo;
    }

    @GetMapping("/ticket-prices")
    public List<TicketPriceResponse> listTicketPrices() {
        List<TicketPriceResponse> responses = new ArrayList<>();
        for (TicketPrice price : ticketPriceRepo.findAllByOrderByDisplayOrderAsc()) {
            responses.add(new TicketPriceResponse(price.getCode(), price.getLabel(), price.getAmount()));
        }
        return responses;
    }
}
