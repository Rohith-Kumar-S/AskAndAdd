package com.orbit.order_service.dto;

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
