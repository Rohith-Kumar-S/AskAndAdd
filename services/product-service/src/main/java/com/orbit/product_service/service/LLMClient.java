package com.orbit.product_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.orbit.product_service.dto.LLMResponse;

@FeignClient(name = "llm-service", url = "http://127.0.0.1:8000")
public interface LLMClient {
	@GetMapping("/extract")
	LLMResponse extractDetailsFromPrompt(@RequestParam String query);
	
	@GetMapping("/rank")
	Map<String, Float> rankProducts(@RequestParam String query, @RequestParam("index_name") String indexName);
}
