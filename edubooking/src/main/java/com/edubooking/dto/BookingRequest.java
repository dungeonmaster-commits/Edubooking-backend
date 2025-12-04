package com.edubooking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private long resourceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
}

