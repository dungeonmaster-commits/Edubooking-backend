package com.edubooking.controller;

import com.edubooking.dto.common.PaginatedResponse;
import com.edubooking.dto.resource.CreateResourceRequest;
import com.edubooking.dto.resource.ResourceResponse;
import com.edubooking.dto.resource.UpdateResourceRequest;
import com.edubooking.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }
    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<ResourceResponse> createResource(
            @RequestPart("data") CreateResourceRequest request,
            @RequestPart("image") MultipartFile image
    ) throws Exception {

        return ResponseEntity.ok(resourceService.createResource(request, image));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long id,
            @RequestPart("data") UpdateResourceRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {

        return ResponseEntity.ok(resourceService.updateResource(id, request, image));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(@PathVariable Long id){
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ResourceResponse>> getResources(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "minCapacity", required = false) Integer minCapacity,
            @RequestParam(value = "maxCapacity", required = false) Integer maxCapacity,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        PaginatedResponse<ResourceResponse> response = resourceService.getResources(
                page, size, search, type, status, minCapacity, maxCapacity, location, sort
        );
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.ok("Resource deleted successfully");
    }
}
