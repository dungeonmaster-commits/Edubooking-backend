package com.edubooking.service.impl;

import com.edubooking.dto.BookingRequest;
import com.edubooking.model.Booking;
import com.edubooking.model.BookingStatus;
import com.edubooking.model.Resource;
import com.edubooking.model.User;
import com.edubooking.repository.BookingRepository;
import com.edubooking.repository.ResourceRepository;
import com.edubooking.repository.UserRepository;
import com.edubooking.service.BookingService;
import com.edubooking.service.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final EmailService emailService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ResourceRepository resourceRepository, EmailService emailService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.emailService = emailService;
    }

    // ------------------------------------------------------------
    // CREATE BOOKING (User request)
    // ------------------------------------------------------------
    @Override
    public Booking createBooking(Long userId, BookingRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        if (start.isAfter(end)) {
            throw new RuntimeException("Start time cannot be after end time");
        }

        // 1️⃣ Check if resource is already booked in this time
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(resource, start, end);
        if (!overlaps.isEmpty()) {
            throw new RuntimeException("This time slot is already booked for the selected resource.");
        }

        // 2️⃣ Check if user already has a booking at this time
        List<Booking> userOverlaps =
                bookingRepository.findOverlappingUserBookings(userId, start, end);

        if (!userOverlaps.isEmpty()) {
            throw new RuntimeException("You already have another booking in this time slot.");
        }

        // 3️⃣ Create new booking request
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setResource(resource);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setPurpose(request.getPurpose());
        booking.setStatus(BookingStatus.PENDING); // initially pending approval

        return bookingRepository.save(booking);
    }

    // ------------------------------------------------------------
    // GET MY BOOKINGS
    // ------------------------------------------------------------
    @Override
    public List<Booking> getMyBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // ------------------------------------------------------------
    // GET ALL BOOKINGS (Admin)
    // ------------------------------------------------------------
    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // ------------------------------------------------------------
    // APPROVE BOOKING (Admin)
    // ------------------------------------------------------------
    @Override
    public Booking approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // re-check overlap before approval
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                booking.getResource(),
                booking.getStartTime(),
                booking.getEndTime()
        );

        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Cannot approve. Slot is already booked.");
        }

        booking.setStatus(BookingStatus.APPROVED);
        Booking saved = bookingRepository.save(booking);

        // ⭐ Send email notification
        emailService.sendEmail(
                booking.getUser().getEmail(),
                "Your Booking Has Been Approved",
                "Hello " + booking.getUser().getName() +
                        ",\n\nYour booking for '" + booking.getResource().getName() +
                        "' from " + booking.getStartTime() +
                        " to " + booking.getEndTime() +
                        " has been approved.\n\nThank you!"
        );

        return saved;
    }

    // ------------------------------------------------------------
    // REJECT BOOKING (Admin)
    // ------------------------------------------------------------
    @Override
    public Booking rejectBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);

        // ⭐ Send rejection email
        emailService.sendEmail(
                booking.getUser().getEmail(),
                "Your Booking Request Was Rejected",
                "Hello " + booking.getUser().getName() +
                        ",\n\nYour booking request for '" + booking.getResource().getName() +
                        "' from " + booking.getStartTime() +
                        " to " + booking.getEndTime() +
                        " has been rejected.\n\nPlease choose another time slot."
        );

        return saved;
    }

    // ------------------------------------------------------------
    // CANCEL BOOKING (User or Admin)
    // ------------------------------------------------------------
    @Override
    public void cancelBooking(Long bookingId, Long userId, boolean isAdmin) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only admin OR the user who created the booking can cancel
        if (!isAdmin && !booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot cancel someone else's booking.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    public boolean isAvailable(Long resourceId, LocalDateTime start, LocalDateTime end) {

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        List<Booking> overlaps = bookingRepository.findOverlappingBookings(resource, start, end);

        return overlaps.isEmpty(); // available if no overlaps
    }
}
