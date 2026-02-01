package com.orbit.product_service.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.orbit.product_service.model.SubCategory;

@Repository
public interface SubCategoryServiceRepo extends JpaRepository<SubCategory, Integer>{

}
