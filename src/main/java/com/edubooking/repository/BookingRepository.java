package com.edubooking.repository;

import com.edubooking.model.Booking;
import com.edubooking.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find already-approved bookings that overlap with a given time slot.
     *
     * (start1 < end2) AND (end1 > start2)
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.resource = :resource
        AND b.status = 'APPROVED'
        AND b.startTime < :end
        AND b.endTime > :start
    """)
    List<Booking> findOverlappingBookings(
            @Param("resource") Resource resource,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    /**
     * Find overlapping bookings for the SAME user
     * This prevents double-booking by the user.
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.user.id = :userId
        AND b.status = 'APPROVED'
        AND b.startTime < :end
        AND b.endTime > :start
    """)
    List<Booking> findOverlappingUserBookings(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    /**
     * Get all bookings by user
     */
    List<Booking> findByUserId(Long userId);
}
