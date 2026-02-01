package com.orbit.product_service.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.orbit.product_service.dto.ListResponse;
import com.orbit.product_service.dto.Product;
import com.orbit.product_service.dto.ProductInternalDto;
import com.orbit.product_service.dto.ProductsResponse;

@RequestMapping("/api")
public interface ProductControllerInterface {

	@GetMapping("/product/id/{productId}")
	ResponseEntity<Product> getProductById(@PathVariable("productId") String productId);

	@GetMapping("/product/title/{productTitle}")
	public ResponseEntity<Page<Product>> getProductByTitle(@PathVariable("productTitle") String productTitle,
			@RequestParam(defaultValue = "featured") String sortBy,
			@RequestParam(defaultValue = "ASC") String sortDirection, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer productCount);

	@GetMapping("/product/filtered")
	public ResponseEntity<Page<Product>> getFilteredProducts(@RequestParam(defaultValue = "") String productName,
			@RequestParam(defaultValue = "") String brand, @RequestParam String category,
			@RequestParam(defaultValue = "") String subCategory, @RequestParam(defaultValue = "featured") String sortBy,
			@RequestParam(defaultValue = "ASC") String sortDirection, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer productCount);

	@GetMapping("/products/by-prompt")
	public ResponseEntity<ProductsResponse> getFilteredProductsFromPrompt(@RequestParam(defaultValue = "") String prompt,
			@RequestParam(defaultValue = "featured") String sortBy,
			@RequestParam(defaultValue = "ASC") String sortDirection, @RequestParam(defaultValue = "0") Integer page,
			@RequestParam(defaultValue = "10") Integer productCount);

	@GetMapping("/products")
	ResponseEntity<List<Product>> getProductsByIds(@RequestParam List<String> ids);

	@GetMapping("/products/popular")
	ResponseEntity<ProductsResponse> getPopularProducts(@RequestParam(defaultValue = "0") Integer sellerId,
			@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer productCount);

	@GetMapping("/product/categories")
	ResponseEntity<ListResponse> getCategories(@RequestParam(defaultValue = "0") Integer sellerId);

	@GetMapping("/product/sellers")
	ResponseEntity<ListResponse> getSellers();

	@GetMapping("/product/similar")
	ResponseEntity<ProductsResponse> getSimilarProducts(@RequestParam(defaultValue = "0") Integer sellerId,
			@RequestParam String productId);
}
