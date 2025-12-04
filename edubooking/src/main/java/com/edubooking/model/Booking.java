package com.edubooking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="bookings")
public class Booking {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

//who booked it?
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;


    // Which resource is booked?
    @ManyToOne
    @JoinColumn(name="resource_id")
    private Resource resource;


    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private String purpose;
}
