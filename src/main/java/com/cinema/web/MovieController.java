package com.cinema.web;

import com.cinema.domain.entity.Movie;
import com.cinema.domain.entity.Screening;
import com.cinema.infrastructure.persistence.repository.MovieRepository;
import com.cinema.web.dto.MovieResponse;
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
    public List<MovieResponse> listMovies() {
        return convertToResponse(movieRepo.findAll());
    }
    
    @GetMapping("/movies/search")
    public List<MovieResponse> searchMovies(@RequestParam String query) {
        // Query mask implementation: search movies by title or genre
        List<Movie> all = movieRepo.findAll();
        List<Movie> filtered = new ArrayList<>();
        String q = query.toLowerCase();
        for (Movie m : all) {
            if (m.getTitle().toLowerCase().contains(q) || m.getGenre().toLowerCase().contains(q)) {
                filtered.add(m);
            }
        }
        return convertToResponse(filtered);
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
