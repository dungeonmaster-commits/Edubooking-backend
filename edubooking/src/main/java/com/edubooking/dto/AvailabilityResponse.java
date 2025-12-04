package com.edubooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvailabilityResponse {
    private Long resourceId;
    private boolean available;
    private String message;
}
