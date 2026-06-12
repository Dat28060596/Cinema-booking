package com.cinema.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "screenings")
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private LocalDate screeningDate;

    private String dateLabel;
    
    @Column(nullable = false)
    private String screeningTime;

    @Column(nullable = false)
    private String hall;

    private String format;

    public Screening() {}

    public Screening(LocalDate screeningDate, String dateLabel, String screeningTime, String hall, String format) {
        this.screeningDate = screeningDate;
        this.dateLabel = dateLabel;
        this.screeningTime = screeningTime;
        this.hall = hall;
        this.format = format;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public LocalDate getScreeningDate() { return screeningDate; }
    public void setScreeningDate(LocalDate screeningDate) { this.screeningDate = screeningDate; }

    public String getDateLabel() { return dateLabel; }
    public void setDateLabel(String dateLabel) { this.dateLabel = dateLabel; }

    public String getScreeningTime() { return screeningTime; }
    public void setScreeningTime(String screeningTime) { this.screeningTime = screeningTime; }

    public String getHall() { return hall; }
    public void setHall(String hall) { this.hall = hall; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
