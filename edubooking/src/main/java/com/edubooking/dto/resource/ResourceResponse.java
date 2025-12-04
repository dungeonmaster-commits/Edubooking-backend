package com.edubooking.dto.resource;

import com.edubooking.model.ResourceStatus;
import com.edubooking.model.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {

    private long id;
    private String name;
    private String description;
    private ResourceType type;
    private Integer capacity;
    private String location;
    private ResourceStatus status;
    private String features;

    private String availabilitySchedule;

    // ⭐ Image URL
    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
