package com.orbit.product_service.dto;

import java.math.BigDecimal;

import com.orbit.product_service.model.Category;
import com.orbit.product_service.model.Seller;
import com.orbit.product_service.model.SubCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	private String id;
	private String productName;

	
	private String description;

	
	private Double initialPrice;

	
	private Double finalPrice;

	
	private Double discount;

	
	private String currency;

	
	private Double rating;

	
	private Integer reviewCount = 0;

	
	private Boolean availableForDelivery;

	
	private String brand;

	
	private String seller;

	private String mainImage;

	private String category;

	private String subCategory;

	private float relevanceScore = -1000.0f;
}	
