package com.cinema.domain.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private String title;

    private int rowsCount;
    private int seatsPerRow;
    private String posterUrl;
    private String genre;
    private int duration;
    private String rating;
    private int releaseYear;

    @Column(length = 1000)
    private String synopsis;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Screening> screenings = new ArrayList<>();

    public Movie() {}

    public Movie(String id, String title, int rowsCount, int seatsPerRow, String posterUrl, String genre, int duration, String rating, int releaseYear, String synopsis) {
        this.id = id;
        this.title = title;
        this.rowsCount = rowsCount;
        this.seatsPerRow = seatsPerRow;
        this.posterUrl = posterUrl;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.releaseYear = releaseYear;
        this.synopsis = synopsis;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getRowsCount() { return rowsCount; }
    public void setRowsCount(int rowsCount) { this.rowsCount = rowsCount; }

    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public List<Screening> getScreenings() { return screenings; }
    public void setScreenings(List<Screening> screenings) { this.screenings = screenings; }
    
    public void addScreening(Screening screening) {
        screenings.add(screening);
        screening.setMovie(this);
    }
}
