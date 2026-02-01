package com.orbit.cart_service.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.orbit.cart_service.dto.Cart;



@FeignClient(name = "order-service", url = "http://localhost:8082/api/order")
public interface OrderClient {
	@PostMapping
	String proceedToBuy(@RequestBody Cart cart);
}
