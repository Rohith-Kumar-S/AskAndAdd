package com.orbit.product_service.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.orbit.product_service.model.Category;

@Repository
public interface CategoryServiceRepo extends JpaRepository<Category, Integer> {

}
