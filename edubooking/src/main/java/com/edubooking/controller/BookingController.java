package com.edubooking.controller;

import com.edubooking.dto.BookingRequest;
import com.edubooking.dto.BookingResponse;
import com.edubooking.dto.AvailabilityResponse;
import com.edubooking.model.Booking;
import com.edubooking.model.BookingStatus;
import com.edubooking.service.BookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@CrossOrigin
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    // ---------------------------------------------------------------
    // 1. CREATE BOOKING (User)
    // ---------------------------------------------------------------
    @PostMapping("/create")
    public ResponseEntity<BookingResponse> create(
            @RequestBody BookingRequest request,
            Authentication auth
    ) {
        Long userId = Long.parseLong(auth.getName()); // extracted from JWT

        Booking booking = bookingService.createBooking(userId, request);
        return ResponseEntity.ok(toResponse(booking));
    }


    // ---------------------------------------------------------------
    // 2. GET MY BOOKINGS (User)
    // ---------------------------------------------------------------
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());

        List<BookingResponse> list = bookingService.getMyBookings(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }


    // ---------------------------------------------------------------
    // 3. GET ALL BOOKINGS (Admin Only)
    // ---------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {

        List<BookingResponse> list = bookingService.getAllBookings()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }


    // ---------------------------------------------------------------
    // 4. APPROVE BOOKING (Admin)
    // ---------------------------------------------------------------
    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approve(@PathVariable Long id) {

        Booking booking = bookingService.approveBooking(id);
        return ResponseEntity.ok(toResponse(booking));
    }


    // ---------------------------------------------------------------
    // 5. REJECT BOOKING (Admin)
    // ---------------------------------------------------------------
    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> reject(@PathVariable Long id) {

        Booking booking = bookingService.rejectBooking(id);
        return ResponseEntity.ok(toResponse(booking));
    }


    // ---------------------------------------------------------------
    // 6. CANCEL BOOKING (User or Admin)
    // ---------------------------------------------------------------
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long id,
            Authentication auth
    ) {

        Long userId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        bookingService.cancelBooking(id, userId, isAdmin);

        return ResponseEntity.ok("Booking cancelled.");
    }


    // ---------------------------------------------------------------
    // 7. AVAILABILITY API (Public)
    // ---------------------------------------------------------------
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestParam Long resourceId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end
    ) {

        boolean available = bookingService.isAvailable(resourceId, start, end);

        if (available) {
            return ResponseEntity.ok(
                    new AvailabilityResponse(resourceId, true,
                            "Resource is available for the selected time.")
            );
        }

        return ResponseEntity.ok(
                new AvailabilityResponse(resourceId, false,
                        "Resource is NOT available for the selected time.")
        );
    }


    // ---------------------------------------------------------------
    // Helper mapper
    // ---------------------------------------------------------------
    private BookingResponse toResponse(Booking b) {
        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        r.setUserId(b.getUser().getId());
        r.setResourceId(b.getResource().getId());
        r.setResourceName(b.getResource().getName());
        r.setStartTime(b.getStartTime());
        r.setEndTime(b.getEndTime());
        r.setPurpose(b.getPurpose());
        r.setStatus(b.getStatus());
        return r;
    }
}
