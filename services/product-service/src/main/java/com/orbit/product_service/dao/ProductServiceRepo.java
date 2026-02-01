package com.orbit.product_service.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.orbit.product_service.model.Product;

@Repository
public interface ProductServiceRepo extends JpaRepository<Product, String> {

	Optional<Product> findByIdAndSellerId(String id, Integer sellerId);

	@Query("SELECT p from Product p WHERE lower(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) AND p.seller.id=:sellerId")
	Page<Product> findByProductNameAndSellerId(@Param("productName") String productName,
			@Param("sellerId") Integer sellerId, Pageable pageable);

	Page<Product> findBySellerId(Integer sellerId, Pageable pageable);

	@Query("SELECT p FROM Product p  WHERE p.seller.id=:sellerId AND p.category.id=:categoryId ORDER BY p.rating DESC, p.reviewCount DESC LIMIT 12")
	List<Product> findSimilarProducts(@Param("sellerId") Integer sellerId, @Param("categoryId") Integer categoryId,
			Pageable pageable);

	Page<Product> findByCategoryName(String categoryName, Pageable pageable);
	
	Page<Product> findBySubCategoryName(String subCategoryName, Pageable pageable);

	Page<Product> findByCategoryNameAndSubCategoryName(String categoryName, String subCategoryName, Pageable pageable);

	Page<Product> findByCategoryNameAndSubCategoryNameAndBrandContainingIgnoreCase(String categoryName, String subCategoryName,
			String brand, Pageable pageable);

	Page<Product> findByCategoryNameAndSubCategoryNameAndBrandContainingIgnoreCaseAndProductNameContainingIgnoreCase(String categoryName,
			String subCategoryName, String brand, String productName, Pageable pageable);
	
	
	Page<Product> findByCategoryNameAndBrandContaining(
	        String categoryName, String brand, Pageable pageable);
	    
    Page<Product> findByCategoryNameAndProductNameContaining(
        String categoryName, String productName, Pageable pageable);
    
    Page<Product> findByCategoryNameAndBrandContainingAndProductNameContaining(
        String categoryName, String brand, String productName, Pageable pageable);
    
    Page<Product> findByCategoryNameAndSubCategoryNameAndProductNameContainingIgnoreCase(
        String categoryName, String subCategoryName, String productName, Pageable pageable);

	Page<Product> findByCategoryNameAndProductNameContainingIgnoreCase(String category,
			String productName, Pageable pageable);

	Page<Product> findByCategoryNameAndBrandContainingIgnoreCase(String category, String brand, Pageable pageable);

}
