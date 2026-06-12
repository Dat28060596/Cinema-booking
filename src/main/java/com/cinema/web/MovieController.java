package com.cinema.web;

import com.cinema.domain.entity.Movie;
import com.cinema.domain.entity.Screening;
import com.cinema.infrastructure.persistence.repository.MovieRepository;
import com.cinema.web.dto.MovieResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MovieController {

    private final MovieRepository movieRepo;

    public MovieController(MovieRepository movieRepo) {
        this.movieRepo = movieRepo;
    }

    @GetMapping("/movies")
    @Transactional(readOnly = true)
    public List<MovieResponse> listMovies() {
        return convertToResponse(movieRepo.findAll());
    }
    
    @GetMapping("/movies/search")
    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(@RequestParam(defaultValue = "") String query) {
        // Query mask: search movies and their showtime fields from one form.
        List<Movie> all = movieRepo.findAll();
        if (query == null || query.isBlank()) {
            return convertToResponse(all);
        }

        List<Movie> filtered = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Movie m : all) {
            if (matchesMovie(m, q)) {
                filtered.add(m);
            }
        }
        return convertToResponse(filtered);
    }

    private boolean matchesMovie(Movie movie, String query) {
        if (contains(movie.getTitle(), query)
                || contains(movie.getGenre(), query)
                || contains(movie.getRating(), query)
                || contains(String.valueOf(movie.getReleaseYear()), query)) {
            return true;
        }

        for (Screening screening : movie.getScreenings()) {
            if (contains(screening.getDateLabel(), query)
                    || contains(screening.getScreeningDate().toString(), query)
                    || contains(screening.getScreeningTime(), query)
                    || contains(screening.getHall(), query)
                    || contains(screening.getFormat(), query)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private List<MovieResponse> convertToResponse(List<Movie> movies) {
        List<MovieResponse> list = new ArrayList<>();
        for (Movie m : movies) {
            List<MovieResponse.Screening> sList = new ArrayList<>();
            for (Screening s : m.getScreenings()) {
                sList.add(new MovieResponse.Screening(
                        s.getScreeningDate().toString(),
                        s.getDateLabel(),
                        s.getScreeningTime(),
                        s.getHall(),
                        s.getFormat(),
                        s.getId()
                ));
            }
            list.add(new MovieResponse(
                    m.getId(), m.getTitle(), m.getRowsCount(), m.getSeatsPerRow(),
                    m.getPosterUrl(), m.getGenre(), m.getDuration(),
                    m.getRating(), m.getReleaseYear(), m.getSynopsis(), sList
            ));
        }
        return list;
    }
}
