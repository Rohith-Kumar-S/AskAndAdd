package com.orbit.product_service.controller;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.orbit.product_service.dto.ListResponse;
import com.orbit.product_service.dto.Product;
import com.orbit.product_service.dto.ProductInternalDto;
import com.orbit.product_service.dto.ProductsResponse;
import com.orbit.product_service.service.ProductServiceInterface;

@RestController
public class ProductControllerImpl implements ProductControllerInterface {

	private final ProductServiceInterface productService;

	@Autowired
	public ProductControllerImpl(ProductServiceInterface productService) {
		this.productService = productService;
	}

	@Override
	public ResponseEntity<Product> getProductById(String productId) {

		Integer sellerId = this.productService.getActiveSeller();
		if (Objects.nonNull(sellerId)) {
			return ResponseEntity.ok(this.productService.getProduct(productId, sellerId));
		} else {
			return ResponseEntity.ok(new Product());
		}
	}

	@Override
	public ResponseEntity<List<Product>> getProductsByIds(List<String> ids) {
		System.out.println("received product list: " + ids.get(0));
		List<Product> products = this.productService.getAllProductsByIds(ids);
		System.out.println("outgoing product list: " + products);
		if (!products.isEmpty()) {
			return ResponseEntity.ok(products);
		}
		return ResponseEntity.ok(Collections.emptyList()); // Don't return null
	}

	@Override
	public ResponseEntity<Page<Product>> getProductByTitle(String productTitle, String sortBy, String sortDirection,
			Integer page, Integer productCount) {

		Integer sellerId = this.productService.getActiveSeller();
		if (Objects.nonNull(sellerId)) {
			return ResponseEntity.ok(this.productService.getProductbyTitle(productTitle, sellerId, sortBy,
					sortDirection, page, productCount));
		} else {
			return null;
		}
	}

	@Override
	public ResponseEntity<ProductsResponse> getPopularProducts(Integer sellerId, Integer page, Integer productCount) {
		if (Objects.nonNull(sellerId)) {
			return ResponseEntity.ok(new ProductsResponse(Boolean.TRUE,
					this.productService.getPopularProducts(sellerId, page, productCount)));
		} else {
			return null;
		}
	}

	@Override
	public ResponseEntity<ListResponse> getCategories(Integer sellerId) {
		// TODO Auto-generated method stub
		if (Objects.nonNull(sellerId)) {
			return ResponseEntity.ok(productService.getCategories(sellerId));
		} else {
			return null;
		}
	}

	@Override
	public ResponseEntity<ListResponse> getSellers() {
		// TODO Auto-generated method stub
		return ResponseEntity.ok(productService.getSellers());
	}

	@Override
	public ResponseEntity<ProductsResponse> getSimilarProducts(Integer sellerId, String productId) {
		// TODO Auto-generated method stub
		return ResponseEntity.ok(productService.getSimilarProducts(sellerId, productId));
	}

	@Override
	public ResponseEntity<Page<Product>> getFilteredProducts(String productName, String brand, String category,
			String subCategory, String sortBy, String sortDirection, Integer page, Integer productCount) {
		// TODO Auto-generated method stub
		Integer sellerId = this.productService.getActiveSeller();
		if (Objects.nonNull(sellerId)) {
			return ResponseEntity.ok(this.productService.getFilteredProducts(productName.isEmpty() ? null : productName,
					brand.isEmpty() ? null : brand,
					category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase(),
					subCategory.isEmpty() ? null : subCategory, sellerId, sortBy, sortDirection, page, productCount));
		} else {
			return null;
		}
	}

	@Override
	public ResponseEntity<ProductsResponse> getFilteredProductsFromPrompt(String prompt, String sortBy,
			String sortDirection, Integer pageNumber, Integer productCount) {
		// TODO Auto-generated method stub
		Integer sellerId = this.productService.getActiveSeller();
		if (Objects.nonNull(sellerId)) {
			return ResponseEntity.ok(new ProductsResponse(Boolean.TRUE,this.productService.getFilteredProductsFromPrompt(prompt, sortBy, sortDirection, pageNumber, productCount)));
		} else {
			return null;
		}
	}

}
