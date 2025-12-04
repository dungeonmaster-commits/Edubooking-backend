package com.edubooking.repository.spec;

import com.edubooking.model.Resource;
import com.edubooking.model.ResourceStatus;
import com.edubooking.model.ResourceType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public final class ResourceSpecifications {

    private ResourceSpecifications() {}

    public static Specification<Resource> hasType(ResourceType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Resource> hasStatus(ResourceStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Resource> capacityGreaterOrEq(Integer minCapacity) {
        return (root, query, cb) -> minCapacity == null ? null : cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity);
    }

    public static Specification<Resource> capacityLessOrEq(Integer maxCapacity) {
        return (root, query, cb) -> maxCapacity == null ? null : cb.lessThanOrEqualTo(root.get("capacity"), maxCapacity);
    }

    public static Specification<Resource> locationContains(String location) {
        return (root, query, cb) -> {
            if (location == null || location.isBlank()) return null;
            String pattern = "%" + location.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("location")), pattern);
        };
    }

    public static Specification<Resource> searchKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
            Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
            Predicate featuresLike = cb.like(cb.lower(root.get("features")), pattern);
            return cb.or(nameLike, descLike, featuresLike);
        };
    }
}
