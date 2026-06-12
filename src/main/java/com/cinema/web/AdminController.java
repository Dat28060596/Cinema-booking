package com.cinema.web;

import com.cinema.application.BookingService;
import com.cinema.domain.entity.AppUser;
import com.cinema.domain.entity.Booking;
import com.cinema.domain.entity.Movie;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.Seat;
import com.cinema.domain.entity.Ticket;
import com.cinema.domain.entity.TicketPrice;
import com.cinema.domain.repository.AppUserRepository;
import com.cinema.infrastructure.persistence.repository.BookingRepository;
import com.cinema.infrastructure.persistence.repository.MovieRepository;
import com.cinema.infrastructure.persistence.repository.ScreeningRepository;
import com.cinema.infrastructure.persistence.repository.SeatRepository;
import com.cinema.infrastructure.persistence.repository.TicketPriceRepository;
import com.cinema.web.dto.AdminBookingResponse;
import com.cinema.web.dto.AdminReportResponse;
import com.cinema.web.dto.AdminScreeningResponse;
import com.cinema.web.dto.AdminSeatHoldResponse;
import com.cinema.web.dto.MovieRequest;
import com.cinema.web.dto.MovieResponse;
import com.cinema.web.dto.RevenueByMovieResponse;
import com.cinema.web.dto.ScreeningRequest;
import com.cinema.web.dto.TicketPriceRequest;
import com.cinema.web.dto.TicketPriceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class AdminController {

    private final BookingService bookingService;
    private final AppUserRepository appUserRepo;
    private final MovieRepository movieRepo;
    private final ScreeningRepository screeningRepo;
    private final SeatRepository seatRepo;
    private final TicketPriceRepository ticketPriceRepo;
    private final BookingRepository bookingRepo;

    public AdminController(
            BookingService bookingService,
            AppUserRepository appUserRepo,
            MovieRepository movieRepo,
            ScreeningRepository screeningRepo,
            SeatRepository seatRepo,
            TicketPriceRepository ticketPriceRepo,
            BookingRepository bookingRepo) {
        this.bookingService = bookingService;
        this.appUserRepo = appUserRepo;
        this.movieRepo = movieRepo;
        this.screeningRepo = screeningRepo;
        this.seatRepo = seatRepo;
        this.ticketPriceRepo = ticketPriceRepo;
        this.bookingRepo = bookingRepo;
    }

    @GetMapping("/admin/bookings")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listBookings(@RequestHeader("X-User-Id") String userId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        List<AdminBookingResponse> responses = new ArrayList<>();
        for (Booking booking : bookingService.listConfirmedBookings()) {
            responses.add(toResponse(booking));
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/bookings/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchBookings(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "") String query) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        String q = normalizeQuery(query);
        List<AdminBookingResponse> responses = new ArrayList<>();
        for (Booking booking : bookingService.listConfirmedBookings()) {
            if (q.isBlank() || matchesBooking(booking, q)) {
                responses.add(toResponse(booking));
            }
        }
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/admin/bookings/{bookingId}")
    public ResponseEntity<?> deleteBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long bookingId) {

        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/held-seats")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listHeldSeats(@RequestHeader("X-User-Id") String userId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        List<AdminSeatHoldResponse> responses = new ArrayList<>();
        for (Seat seat : bookingService.listHeldSeats()) {
            responses.add(toResponse(seat));
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/held-seats/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchHeldSeats(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "") String query) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        String q = normalizeQuery(query);
        List<AdminSeatHoldResponse> responses = new ArrayList<>();
        for (Seat seat : bookingService.listHeldSeats()) {
            if (q.isBlank() || matchesHeldSeat(seat, q)) {
                responses.add(toResponse(seat));
            }
        }
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/admin/held-seats")
    public ResponseEntity<?> releaseHeldSeats(@RequestHeader("X-User-Id") String userId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        int released = bookingService.releaseHeldSeats();
        return ResponseEntity.ok(Map.of("released", released));
    }

    @GetMapping("/admin/reports/revenue")
    @Transactional(readOnly = true)
    public ResponseEntity<?> revenueReport(@RequestHeader("X-User-Id") String userId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long confirmedBookings = 0;
        for (Booking booking : bookingRepo.findAll()) {
            if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
                confirmedBookings++;
                totalRevenue = totalRevenue.add(booking.getTotalAmount());
            }
        }

        List<RevenueByMovieResponse> revenueByMovie = new ArrayList<>();
        for (Object[] row : bookingRepo.findRevenueByMovie()) {
            revenueByMovie.add(new RevenueByMovieResponse((String) row[0], (BigDecimal) row[1]));
        }

        return ResponseEntity.ok(new AdminReportResponse(
                totalRevenue,
                confirmedBookings,
                bookingService.listHeldSeats().size(),
                revenueByMovie
        ));
    }

    @GetMapping("/admin/movies")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listMovies(@RequestHeader("X-User-Id") String userId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        List<MovieResponse> responses = new ArrayList<>();
        for (Movie movie : movieRepo.findAll()) {
            responses.add(toMovieResponse(movie));
        }
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/admin/movies")
    public ResponseEntity<?> createMovie(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody MovieRequest req) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        validateMovieRequest(req);
        String movieId = isBlank(req.id()) ? generateMovieId(req.title()) : req.id().trim();
        if (movieRepo.existsById(movieId)) {
            throw new IllegalArgumentException("Movie ID already exists");
        }

        Movie movie = new Movie();
        movie.setId(movieId);
        applyMovieRequest(movie, req, true);
        movieRepo.save(movie);

        return ResponseEntity.status(HttpStatus.CREATED).body(toMovieResponse(movie));
    }

    @PutMapping("/admin/movies/{movieId}")
    @Transactional
    public ResponseEntity<?> updateMovie(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String movieId,
            @RequestBody MovieRequest req) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        validateMovieRequest(req);
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        boolean hasScreenings = !screeningRepo.findByMovieId(movieId).isEmpty();
        applyMovieRequest(movie, req, !hasScreenings);

        return ResponseEntity.ok(toMovieResponse(movieRepo.save(movie)));
    }

    @DeleteMapping("/admin/movies/{movieId}")
    public ResponseEntity<?> deleteMovie(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String movieId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        if (!screeningRepo.findByMovieId(movieId).isEmpty()) {
            throw new IllegalArgumentException("Delete this movie's screenings before deleting the movie");
        }

        movieRepo.delete(movie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/screenings")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listScreenings(@RequestHeader("X-User-Id") String userId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        List<AdminScreeningResponse> responses = new ArrayList<>();
        for (Screening screening : screeningRepo.findAll()) {
            responses.add(toAdminScreeningResponse(screening));
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/admin/screenings/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchScreenings(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "") String query) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        String q = normalizeQuery(query);
        List<AdminScreeningResponse> responses = new ArrayList<>();
        for (Screening screening : screeningRepo.findAll()) {
            if (q.isBlank() || matchesScreening(screening, q)) {
                responses.add(toAdminScreeningResponse(screening));
            }
        }
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/admin/screenings")
    @Transactional
    public ResponseEntity<?> createScreening(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody ScreeningRequest req) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        validateScreeningRequest(req);
        Movie movie = movieRepo.findById(req.movieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        Screening screening = new Screening(
                req.screeningDate(),
                resolveDateLabel(req.screeningDate(), req.dateLabel()),
                req.screeningTime().trim(),
                req.hall().trim(),
                cleanOptional(req.format())
        );
        screening.setMovie(movie);
        screening = screeningRepo.save(screening);
        createSeatsForScreening(movie, screening);

        return ResponseEntity.status(HttpStatus.CREATED).body(toAdminScreeningResponse(screening));
    }

    @PutMapping("/admin/screenings/{screeningId}")
    @Transactional
    public ResponseEntity<?> updateScreening(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long screeningId,
            @RequestBody ScreeningRequest req) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        validateScreeningRequest(req);
        Screening screening = screeningRepo.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));
        if (!screening.getMovie().getId().equals(req.movieId())) {
            throw new IllegalArgumentException("Movie cannot be changed for an existing screening");
        }

        screening.setScreeningDate(req.screeningDate());
        screening.setDateLabel(resolveDateLabel(req.screeningDate(), req.dateLabel()));
        screening.setScreeningTime(req.screeningTime().trim());
        screening.setHall(req.hall().trim());
        screening.setFormat(cleanOptional(req.format()));

        return ResponseEntity.ok(toAdminScreeningResponse(screeningRepo.save(screening)));
    }

    @DeleteMapping("/admin/screenings/{screeningId}")
    @Transactional
    public ResponseEntity<?> deleteScreening(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long screeningId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        Screening screening = screeningRepo.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found"));
        if (hasBookingHistory(screeningId)) {
            throw new IllegalArgumentException("Screening has booking history and cannot be deleted");
        }

        List<Seat> seats = seatRepo.findByScreeningId(screeningId);
        for (Seat seat : seats) {
            if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                throw new IllegalArgumentException("Release or cancel occupied seats before deleting this screening");
            }
        }
        seatRepo.deleteAll(seats);
        screeningRepo.delete(screening);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/ticket-prices")
    public ResponseEntity<?> listTicketPrices(@RequestHeader("X-User-Id") String userId) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }

        return ResponseEntity.ok(toTicketPriceResponses());
    }

    @PutMapping("/admin/ticket-prices/{code}")
    public ResponseEntity<?> updateTicketPrice(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String code,
            @RequestBody TicketPriceRequest req) {
        if (!isAdmin(userId)) {
            return adminForbidden();
        }
        if (req == null || req.amount() == null || req.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ticket price must be greater than zero");
        }

        TicketPrice ticketPrice = ticketPriceRepo.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));
        ticketPrice.setAmount(req.amount());

        return ResponseEntity.ok(toResponse(ticketPriceRepo.save(ticketPrice)));
    }

    private void validateMovieRequest(MovieRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Movie is required");
        }
        if (isBlank(req.title())) {
            throw new IllegalArgumentException("Movie title is required");
        }
        if (req.rows() <= 0 || req.rows() > 26) {
            throw new IllegalArgumentException("Rows must be between 1 and 26");
        }
        if (req.seatsPerRow() <= 0) {
            throw new IllegalArgumentException("Seats per row must be greater than zero");
        }
        if (req.duration() <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }
        if (req.year() <= 0) {
            throw new IllegalArgumentException("Release year must be greater than zero");
        }
    }

    private void applyMovieRequest(Movie movie, MovieRequest req, boolean allowSeatLayoutChange) {
        if (!allowSeatLayoutChange && (movie.getRowsCount() != req.rows() || movie.getSeatsPerRow() != req.seatsPerRow())) {
            throw new IllegalArgumentException("Seat layout cannot be changed while the movie has screenings");
        }
        movie.setTitle(req.title().trim());
        movie.setRowsCount(req.rows());
        movie.setSeatsPerRow(req.seatsPerRow());
        movie.setPosterUrl(cleanOptional(req.posterUrl()));
        movie.setGenre(cleanOptional(req.genre()));
        movie.setDuration(req.duration());
        movie.setRating(cleanOptional(req.rating()));
        movie.setReleaseYear(req.year());
        movie.setSynopsis(cleanOptional(req.synopsis()));
    }

    private void validateScreeningRequest(ScreeningRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Screening is required");
        }
        if (isBlank(req.movieId())) {
            throw new IllegalArgumentException("Movie is required");
        }
        if (req.screeningDate() == null) {
            throw new IllegalArgumentException("Screening date is required");
        }
        if (isBlank(req.screeningTime())) {
            throw new IllegalArgumentException("Screening time is required");
        }
        if (isBlank(req.hall())) {
            throw new IllegalArgumentException("Hall is required");
        }
    }

    private void createSeatsForScreening(Movie movie, Screening screening) {
        String rowLabels = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int r = 0; r < movie.getRowsCount(); r++) {
            String rowLabel = String.valueOf(rowLabels.charAt(r));
            for (int seatNum = 1; seatNum <= movie.getSeatsPerRow(); seatNum++) {
                String seatId = rowLabel + seatNum;
                seatRepo.save(new Seat(screening, rowLabel, seatNum, seatId));
            }
        }
    }

    private boolean hasBookingHistory(Long screeningId) {
        for (Booking booking : bookingRepo.findAll()) {
            if (booking.getScreening().getId().equals(screeningId)) {
                return true;
            }
        }
        return false;
    }

    private AdminBookingResponse toResponse(Booking booking) {
        List<String> seatIds = new ArrayList<>();
        for (Ticket ticket : booking.getTickets()) {
            seatIds.add(ticket.getSeat().getSeatId());
        }

        String customerName = booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName();
        return new AdminBookingResponse(
                booking.getId(),
                customerName,
                booking.getCustomer().getEmail(),
                booking.getScreening().getMovie().getTitle(),
                booking.getScreening().getScreeningDate().toString(),
                booking.getScreening().getScreeningTime(),
                seatIds,
                booking.getTotalAmount(),
                booking.getStatus().name()
        );
    }

    private boolean matchesBooking(Booking booking, String query) {
        CustomerFields customer = customerFields(booking);
        if (containsIgnoreCase(String.valueOf(booking.getId()), query)
                || containsIgnoreCase(customer.fullName(), query)
                || containsIgnoreCase(customer.email(), query)
                || containsIgnoreCase(booking.getScreening().getMovie().getTitle(), query)
                || containsIgnoreCase(booking.getScreening().getScreeningDate().toString(), query)
                || containsIgnoreCase(booking.getScreening().getScreeningTime(), query)
                || containsIgnoreCase(booking.getStatus().name(), query)
                || containsIgnoreCase(booking.getTotalAmount().toPlainString(), query)) {
            return true;
        }

        for (Ticket ticket : booking.getTickets()) {
            if (containsIgnoreCase(ticket.getSeat().getSeatId(), query)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesHeldSeat(Seat seat, String query) {
        Screening screening = seat.getScreening();
        return containsIgnoreCase(screening.getMovie().getTitle(), query)
                || containsIgnoreCase(screening.getScreeningDate().toString(), query)
                || containsIgnoreCase(screening.getScreeningTime(), query)
                || containsIgnoreCase(seat.getSeatId(), query)
                || containsIgnoreCase(seat.getHeldBySessionId(), query);
    }

    private boolean matchesScreening(Screening screening, String query) {
        return containsIgnoreCase(String.valueOf(screening.getId()), query)
                || containsIgnoreCase(screening.getMovie().getTitle(), query)
                || containsIgnoreCase(screening.getScreeningDate().toString(), query)
                || containsIgnoreCase(screening.getDateLabel(), query)
                || containsIgnoreCase(screening.getScreeningTime(), query)
                || containsIgnoreCase(screening.getHall(), query)
                || containsIgnoreCase(screening.getFormat(), query);
    }

    private CustomerFields customerFields(Booking booking) {
        String firstName = cleanOptional(booking.getCustomer().getFirstName());
        String lastName = cleanOptional(booking.getCustomer().getLastName());
        return new CustomerFields((firstName + " " + lastName).trim(), cleanOptional(booking.getCustomer().getEmail()));
    }

    private AdminSeatHoldResponse toResponse(Seat seat) {
        return new AdminSeatHoldResponse(
                seat.getScreening().getId(),
                seat.getScreening().getMovie().getTitle(),
                seat.getScreening().getScreeningDate().toString(),
                seat.getScreening().getScreeningTime(),
                seat.getSeatId(),
                seat.getHeldBySessionId()
        );
    }

    private AdminScreeningResponse toAdminScreeningResponse(Screening screening) {
        int total = 0;
        int held = 0;
        int booked = 0;
        for (Seat seat : seatRepo.findByScreeningId(screening.getId())) {
            total++;
            if (seat.getStatus() == Seat.SeatStatus.HELD) {
                held++;
            } else if (seat.getStatus() == Seat.SeatStatus.BOOKED) {
                booked++;
            }
        }

        return new AdminScreeningResponse(
                screening.getId(),
                screening.getMovie().getId(),
                screening.getMovie().getTitle(),
                screening.getScreeningDate().toString(),
                screening.getDateLabel(),
                screening.getScreeningTime(),
                screening.getHall(),
                screening.getFormat(),
                total,
                held,
                booked
        );
    }

    private MovieResponse toMovieResponse(Movie movie) {
        List<MovieResponse.Screening> screenings = new ArrayList<>();
        for (Screening screening : movie.getScreenings()) {
            screenings.add(new MovieResponse.Screening(
                    screening.getScreeningDate().toString(),
                    screening.getDateLabel(),
                    screening.getScreeningTime(),
                    screening.getHall(),
                    screening.getFormat(),
                    screening.getId()
            ));
        }

        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getRowsCount(),
                movie.getSeatsPerRow(),
                movie.getPosterUrl(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getRating(),
                movie.getReleaseYear(),
                movie.getSynopsis(),
                screenings
        );
    }

    private List<TicketPriceResponse> toTicketPriceResponses() {
        List<TicketPriceResponse> responses = new ArrayList<>();
        for (TicketPrice ticketPrice : ticketPriceRepo.findAllByOrderByDisplayOrderAsc()) {
            responses.add(toResponse(ticketPrice));
        }
        return responses;
    }

    private TicketPriceResponse toResponse(TicketPrice ticketPrice) {
        return new TicketPriceResponse(ticketPrice.getCode(), ticketPrice.getLabel(), ticketPrice.getAmount());
    }

    private String generateMovieId(String title) {
        String base = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "movie";
        }

        String candidate = base;
        int suffix = 2;
        while (movieRepo.existsById(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String resolveDateLabel(LocalDate date, String requestedLabel) {
        if (!isBlank(requestedLabel)) {
            return requestedLabel.trim();
        }
        LocalDate today = LocalDate.now();
        if (today.equals(date)) {
            return "Today";
        }
        if (today.plusDays(1).equals(date)) {
            return "Tomorrow";
        }
        return date.toString();
    }

    private String cleanOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private ResponseEntity<Map<String, String>> adminForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin access required"));
    }

    private boolean isAdmin(String userId) {
        try {
            Long id = Long.valueOf(userId);
            AppUser user = appUserRepo.findById(id).orElse(null);
            return user != null && "ADMIN".equals(user.getRole());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private record CustomerFields(String fullName, String email) {
    }
}
