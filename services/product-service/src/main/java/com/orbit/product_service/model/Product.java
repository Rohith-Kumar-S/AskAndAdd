package com.orbit.product_service.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

//	@Id
//	String id;
//	@Column(length = 1000)
//	String title;
//	@Column(name = "img_url", length = 1000)
//	String imgUrl;
//	Float rating;
//	@Column(name = "review_count")
//	Integer reviewCount;
//	Double price;
//	@ManyToOne
//	@JoinColumn(name = "category_id", nullable = false)
//	Category category;
//	@ManyToOne
//	@JoinColumn(name = "sub_category_id", nullable = false)
//	Category subCategory;

	@Id
	private String id;

	@Column(name = "product_name", length = 255)
	private String productName;

	@Column(name = "description_", columnDefinition = "TEXT")
	private String description;

	@Column(name = "initial_price", precision = 10, scale = 2)
	private BigDecimal initialPrice;

	@Column(name = "final_price", precision = 10, scale = 2)
	private BigDecimal finalPrice;

	@Column(name = "discount", precision = 5, scale = 2)
	private BigDecimal discount;

	@Column(name = "currency", length = 3)
	private String currency;

	@Column(name = "rating", precision = 2, scale = 1)
	private BigDecimal rating;

	@Column(name = "review_count")
	private Integer reviewCount = 0;

	@Column(name = "available_for_delivery")
	private Boolean availableForDelivery;

	@Column(name = "brand", length = 150)
	private String brand;

	@ManyToOne
	@JoinColumn(name = "seller_id", nullable = false)
	Seller seller;

	@Column(name = "main_image", columnDefinition = "TEXT")
	private String mainImage;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToOne
	@JoinColumn(name = "sub_category_id", nullable = false)
	private SubCategory subCategory;

}
