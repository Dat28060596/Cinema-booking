package com.cinema.infrastructure;

import com.cinema.domain.entity.Movie;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.Seat;
import com.cinema.domain.entity.TicketPrice;
import com.cinema.infrastructure.persistence.repository.MovieRepository;
import com.cinema.infrastructure.persistence.repository.ScreeningRepository;
import com.cinema.infrastructure.persistence.repository.SeatRepository;
import com.cinema.infrastructure.persistence.repository.TicketPriceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final MovieRepository movieRepo;
    private final ScreeningRepository screeningRepo;
    private final SeatRepository seatRepo;
    private final com.cinema.domain.repository.AppUserRepository appUserRepo;
    private final TicketPriceRepository ticketPriceRepo;

    public DatabaseInitializer(MovieRepository movieRepo, ScreeningRepository screeningRepo, SeatRepository seatRepo, com.cinema.domain.repository.AppUserRepository appUserRepo, TicketPriceRepository ticketPriceRepo) {
        this.movieRepo = movieRepo;
        this.screeningRepo = screeningRepo;
        this.seatRepo = seatRepo;
        this.appUserRepo = appUserRepo;
        this.ticketPriceRepo = ticketPriceRepo;
    }

    @Override
    public void run(String... args) {
        if (appUserRepo.count() == 0) {
            appUserRepo.save(new com.cinema.domain.entity.AppUser("admin", "password", "ADMIN"));
            appUserRepo.save(new com.cinema.domain.entity.AppUser("user", "password", "USER"));
        }
        if (ticketPriceRepo.count() == 0) {
            initTicketPrices();
        }
        if (movieRepo.count() == 0) {
            initData();
        }
    }

    private void initTicketPrices() {
        ticketPriceRepo.save(new TicketPrice("normal", "Normal", new BigDecimal("13.00"), 1));
        ticketPriceRepo.save(new TicketPrice("pupils", "Pupils", new BigDecimal("9.00"), 2));
        ticketPriceRepo.save(new TicketPrice("seniors", "Seniors", new BigDecimal("10.00"), 3));
        ticketPriceRepo.save(new TicketPrice("students", "Students", new BigDecimal("10.00"), 4));
        ticketPriceRepo.save(new TicketPrice("child", "Child", new BigDecimal("7.00"), 5));
    }

    private void initData() {
        Movie inception = new Movie("inception", "Inception", 5, 8, "/posters/poster_inception.png", "Science Fiction / Thriller", 148, "Rated 13", 2010, "A skilled thief who steals corporate secrets through dream-sharing technology is given the task of planting an idea into the mind of a CEO.");
        Movie dune = new Movie("dune", "Dune: Part Two", 4, 6, "/posters/poster_dune.png", "Science Fiction / Adventure", 166, "Rated 13", 2024, "Paul Atreides unites with the Fremen while on a warpath of revenge against the conspirators who destroyed his family.");
        Movie interstellar = new Movie("interstellar", "Interstellar", 6, 10, "/posters/poster_interstellar.png", "Science Fiction / Drama", 169, "Rated 12", 2014, "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot is tasked with piloting a spacecraft to find a new planet.");
        Movie batman = new Movie("batman", "The Batman", 5, 8, "/posters/poster_batman.png", "Action / Crime / Drama", 176, "Rated 16", 2022, "When a sadistic serial killer begins murdering key political figures in Gotham, the Batman is forced to investigate the city's hidden corruption.");

        List<Movie> movies = List.of(inception, dune, interstellar, batman);
        movieRepo.saveAll(movies);

        LocalDate today = LocalDate.now();
        String[] times = {"14:15", "16:30", "19:00", "21:45"};
        String[] halls = {"Hall 1", "Hall 2", "Hall 3", "Club B"};
        String[] formats = {"2D ATMOS", "2D", "2D", "2D OV"};
        
        for (Movie m : movies) {
            for (int d = 0; d < 3; d++) {
                LocalDate date = today.plusDays(d);
                String label = d == 0 ? "Today" : d == 1 ? "Tomorrow" : date.toString();
                
                int startTime = d % 3;
                for (int t = startTime; t < startTime + 2 && t < times.length; t++) {
                    Screening s = new Screening(date, label, times[t], halls[t], formats[t]);
                    s.setMovie(m);
                    s = screeningRepo.save(s);

                    // Create Seats
                    String rowLabels = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    for (int r = 0; r < m.getRowsCount(); r++) {
                        String rLabel = String.valueOf(rowLabels.charAt(r));
                        for (int seatNum = 1; seatNum <= m.getSeatsPerRow(); seatNum++) {
                            String seatId = rLabel + seatNum;
                            Seat seat = new Seat(s, rLabel, seatNum, seatId);
                            seatRepo.save(seat);
                        }
                    }
                }
            }
        }
    }
}
