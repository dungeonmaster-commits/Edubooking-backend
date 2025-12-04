package com.edubooking.service;
import com.edubooking.dto.common.PaginatedResponse;
import com.edubooking.dto.resource.CreateResourceRequest;
import com.edubooking.dto.resource.ResourceResponse;
import com.edubooking.dto.resource.UpdateResourceRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
//Create a resource
ResourceResponse createResource(CreateResourceRequest request, MultipartFile image) throws Exception;

    //Update the resource

    ResourceResponse updateResource(Long id,UpdateResourceRequest request, MultipartFile image) throws Exception;

    //Get resource by id

    ResourceResponse getResourceById(Long id);

    // GET all resources
    List<ResourceResponse> getAllResources();


    // Delete a resource

    void deleteResource(Long id);

    PaginatedResponse<ResourceResponse> getResources(
            Integer page,
            Integer size,
            String search,
            String type,
            String status,
            Integer minCapacity,
            Integer maxCapacity,
            String location,
            String sort
    );



}
