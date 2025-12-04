package com.edubooking.service.impl;

import com.edubooking.dto.common.PaginatedResponse;
import com.edubooking.dto.resource.CreateResourceRequest;
import com.edubooking.dto.resource.ResourceResponse;
import com.edubooking.dto.resource.UpdateResourceRequest;
import com.edubooking.model.Resource;
import com.edubooking.model.ResourceType;
import com.edubooking.model.ResourceStatus;

import com.edubooking.repository.ResourceRepository;
import com.edubooking.repository.spec.ResourceSpecifications;

import com.edubooking.service.ImageUploadService;
import com.edubooking.service.ResourceService;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final ImageUploadService imageUploadService;

    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               ImageUploadService imageUploadService) {
        this.resourceRepository = resourceRepository;
        this.imageUploadService = imageUploadService;
    }


    // ----------------------------------------------------------------------
    // CREATE RESOURCE
    // ----------------------------------------------------------------------
    @Override
    public ResourceResponse createResource(CreateResourceRequest dto, MultipartFile image) throws Exception {

        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Image is required");
        }

        // Upload image first
        Map<String, String> upload = imageUploadService.uploadImage(image);

        Resource resource = new Resource();
        resource.setName(dto.getName());
        resource.setDescription(dto.getDescription());
        resource.setType(dto.getType());
        resource.setCapacity(dto.getCapacity());
        resource.setLocation(dto.getLocation());
        resource.setStatus(dto.getStatus());
        resource.setFeatures(dto.getFeatures());
        resource.setAvailabilitySchedule(dto.getAvailabilitySchedule());

        resource.setImageUrl(upload.get("url"));
        resource.setImagePublicId(upload.get("publicId"));

        Resource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }


    // ----------------------------------------------------------------------
    // UPDATE RESOURCE (with optional image replacement)
    // ----------------------------------------------------------------------
    @Override
    public ResourceResponse updateResource(Long id,
                                           UpdateResourceRequest request,
                                           MultipartFile image) throws Exception{

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        // Safe field updates (avoid overriding with null)
        if (request.getName() != null) resource.setName(request.getName());
        if (request.getDescription() != null) resource.setDescription(request.getDescription());
        if (request.getType() != null) resource.setType(request.getType());
        if (request.getCapacity() != null) resource.setCapacity(request.getCapacity());
        if (request.getLocation() != null) resource.setLocation(request.getLocation());
        if (request.getStatus() != null) resource.setStatus(request.getStatus());
        if (request.getFeatures() != null) resource.setFeatures(request.getFeatures());
        if (request.getAvailabilitySchedule() != null)
            resource.setAvailabilitySchedule(request.getAvailabilitySchedule());


        // ⭐ Image replacement flow
        if (image != null && !image.isEmpty()) {

            String oldPublicId = resource.getImagePublicId();

            // 1️⃣ Upload new image first (safe)
            Map<String, String> upload = imageUploadService.uploadImage(image);

            resource.setImageUrl(upload.get("url"));
            resource.setImagePublicId(upload.get("publicId"));

            // 2️⃣ Try deleting old image (not critical)
            if (oldPublicId != null) {
                try {
                    imageUploadService.deleteImage(oldPublicId);
                } catch (Exception e) {
                    System.out.println("⚠ Warning: Failed to delete old Cloudinary image: " + oldPublicId);
                }
            }
        }

        Resource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }


    // ----------------------------------------------------------------------
    // GET RESOURCE BY ID
    // ----------------------------------------------------------------------
    @Override
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found with given id"));
        return mapToResponse(resource);
    }


    // ----------------------------------------------------------------------
    // GET ALL RESOURCES
    // ----------------------------------------------------------------------
    @Override
    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // ----------------------------------------------------------------------
    // DELETE RESOURCE (delete Cloudinary image too)
    // ----------------------------------------------------------------------
    @Override
    public void deleteResource(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        // Delete image from Cloudinary
        if (resource.getImagePublicId() != null) {
            try {
                imageUploadService.deleteImage(resource.getImagePublicId());
            } catch (Exception e) {
                System.out.println("⚠ Warning: Failed to delete resource image: " + resource.getImagePublicId());
            }
        }

        resourceRepository.deleteById(id);
    }

    @Override
    public PaginatedResponse<ResourceResponse> getResources(Integer page,
                                                            Integer size,
                                                            String search,
                                                            String type,
                                                            String status,
                                                            Integer minCapacity,
                                                            Integer maxCapacity,
                                                            String location,
                                                            String sort) {

        int pageNum = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size <= 0) ? 10 : size;

        // parse sort parameter: property,dir  e.g. "capacity,desc"
        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt"); // default
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String prop = parts[0].trim();
            String dir = (parts.length > 1) ? parts[1].trim().toLowerCase() : "desc";
            Sort.Direction direction = "asc".equals(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortObj = Sort.by(direction, prop);
        }

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortObj);

        // Build Specification dynamically
        List<Specification<Resource>> specs = new ArrayList<>();

        // search keyword
        specs.add(ResourceSpecifications.searchKeyword(search));

        // type
        ResourceType resourceType = null;
        if (type != null && !type.isBlank()) {
            try { resourceType = ResourceType.valueOf(type.trim().toUpperCase()); } catch (Exception ignored) {}
        }
        specs.add(ResourceSpecifications.hasType(resourceType));

        // status
        ResourceStatus resourceStatus = null;
        if (status != null && !status.isBlank()) {
            try { resourceStatus = ResourceStatus.valueOf(status.trim().toUpperCase()); } catch (Exception ignored) {}
        }
        specs.add(ResourceSpecifications.hasStatus(resourceStatus));

        // capacity range
        specs.add(ResourceSpecifications.capacityGreaterOrEq(minCapacity));
        specs.add(ResourceSpecifications.capacityLessOrEq(maxCapacity));

        // location contains
        specs.add(ResourceSpecifications.locationContains(location));

        // combine specs
        Specification<Resource> finalSpec = null;
        for (Specification<Resource> s : specs) {
            if (s == null) continue;
            finalSpec = (finalSpec == null) ? Specification.where(s) : finalSpec.and(s);
        }

        Page<Resource> resultPage = resourceRepository.findAll(finalSpec, pageable);

        List<ResourceResponse> content = resultPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isLast()
        );
    }


    // ----------------------------------------------------------------------
    // RESPONSE MAPPER
    // ----------------------------------------------------------------------
    private ResourceResponse mapToResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.getCapacity(),
                resource.getLocation(),
                resource.getStatus(),
                resource.getFeatures(),
                resource.getAvailabilitySchedule(),
                resource.getImageUrl(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
