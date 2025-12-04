package com.edubooking.dto.resource;

import com.edubooking.model.ResourceStatus;
import com.edubooking.model.ResourceType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateResourceRequest {
    private String name;
    private String description;
    private ResourceType type;
    private Integer capacity;
    private String location;
    private ResourceStatus status;
    private String features;
    private String availabilitySchedule;

    private String imageUrl;
}
