package com.orbit.cart_service.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.orbit.cart_service.dto.Product;

@FeignClient(name = "product-service", url = "http://localhost:8083/api/products")
public interface ProductClient {
	@GetMapping
	List<Product> getProductsById(@RequestParam List<String> ids);
}
