package com.edubooking.service;

import com.edubooking.dto.BookingRequest;
import com.edubooking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    Booking createBooking(Long userId, BookingRequest request);

    List<Booking> getMyBookings(Long userId);

    List<Booking> getAllBookings();

    Booking approveBooking(Long bookingId);

    Booking rejectBooking(Long bookingId);

    void cancelBooking(Long bookingId, Long userId, boolean isAdmin);

    boolean isAvailable(Long resourceId, LocalDateTime start, LocalDateTime end);
}
