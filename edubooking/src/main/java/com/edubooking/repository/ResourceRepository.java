package com.edubooking.repository;

import com.edubooking.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ResourceRepository extends JpaRepository<Resource,Long>, JpaSpecificationExecutor<Resource> {
}
