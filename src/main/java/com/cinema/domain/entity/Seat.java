package com.cinema.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @Column(nullable = false, length = 5)
    private String rowLabel;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false, length = 10)
    private String seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    private String heldBySessionId;

    public Seat() {}

    public Seat(Screening screening, String rowLabel, int number, String seatId) {
        this.screening = screening;
        this.rowLabel = rowLabel;
        this.number = number;
        this.seatId = seatId;
    }

    public enum SeatStatus {
        AVAILABLE, HELD, BOOKED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Screening getScreening() { return screening; }
    public void setScreening(Screening screening) { this.screening = screening; }

    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public String getHeldBySessionId() { return heldBySessionId; }
    public void setHeldBySessionId(String heldBySessionId) { this.heldBySessionId = heldBySessionId; }
}
