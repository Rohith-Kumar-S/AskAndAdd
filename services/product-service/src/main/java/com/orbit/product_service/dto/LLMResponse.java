package com.orbit.product_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMResponse {
	private String category;
	@JsonProperty("sub_category")
    private String subCategory;
    private String brand;
    @JsonProperty("vector_search_index")
    private String vectorSearchIndex;
    private String productName;
}
