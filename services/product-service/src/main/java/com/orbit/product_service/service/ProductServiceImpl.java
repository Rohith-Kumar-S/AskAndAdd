package com.orbit.product_service.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.orbit.product_service.dao.CategoryServiceRepo;
import com.orbit.product_service.dao.ProductServiceRepo;
import com.orbit.product_service.dao.SellerServiceRepo;
import com.orbit.product_service.dto.LLMResponse;
import com.orbit.product_service.dto.ListResponse;
import com.orbit.product_service.dto.Product;
import com.orbit.product_service.dto.ProductInternalDto;
import com.orbit.product_service.dto.ProductsResponse;
import com.orbit.product_service.mapper.ProductServiceMapper;
import com.orbit.product_service.model.Category;
import com.orbit.product_service.model.Seller;

import jakarta.transaction.Transactional;

@Service
public class ProductServiceImpl implements ProductServiceInterface {

	private final ProductServiceRepo productServiceRepo;
	private final SellerServiceRepo sellerServiceRepo;
	private final CategoryServiceRepo categoryServiceRepo;
	private ProductServiceMapper productServiceMapper;
	private final LLMClient llmClient;

	@Autowired
	public ProductServiceImpl(ProductServiceRepo productServiceRepo, SellerServiceRepo sellerServiceRepo,
			CategoryServiceRepo categoryServiceRepo, ProductServiceMapper productServiceMapper, LLMClient llmClient) {
		// TODO Auto-generated constructor stub
		this.productServiceRepo = productServiceRepo;
		this.sellerServiceRepo = sellerServiceRepo;
		this.categoryServiceRepo = categoryServiceRepo;
		this.productServiceMapper = productServiceMapper;
		this.llmClient = llmClient;

	}

	@Override
	@Transactional
	public Product getProduct(String id, Integer sellerId) {
		// TODO Auto-generated method stub
		Optional<com.orbit.product_service.model.Product> product_opt = this.productServiceRepo.findByIdAndSellerId(id,
				sellerId);
		if (product_opt.isPresent()) {
			Product product_view = this.productServiceMapper.mapProductModelToView(product_opt.get());
			return product_view;
		} else {
			return new Product();
		}
	}

	@Override
	public Boolean isSellerActive(Integer sellerId) {
		// TODO Auto-generated method stub

		Optional<Seller> sellerOpt = this.sellerServiceRepo.findById(sellerId);
		if (sellerOpt.isPresent()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public Page<Product> getProductbyTitle(String title, Integer sellerId, String sortBy, String sortDirection,
			Integer pageNumber, Integer productCount) {
		// TODO Auto-generated method stub

		Sort sort = Sort.unsorted();
		switch (sortBy) {
		case "price":
			sort = Sort.by(sortBy == "ASC" ? Sort.Direction.ASC : Sort.Direction.DESC, "price");
			break;
		case "rating":
			sort = Sort.by(sortBy == "ASC" ? Sort.Direction.ASC : Sort.Direction.DESC, "rating");
			break;
		default:
			break;
		}

		Pageable pageable = PageRequest.of(pageNumber, productCount, sort);
		Page<com.orbit.product_service.model.Product> productsPageModel = this.productServiceRepo
				.findByProductNameAndSellerId(title, sellerId, pageable);
		Page<Product> productsView = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		return productsView;
	}

	@Override
	public List<Product> getAllProductsByIds(List<String> productIds) {
		List<com.orbit.product_service.model.Product> products = this.productServiceRepo.findAllById(productIds);
		return products.stream().map(this.productServiceMapper::mapProductModelToView).collect(Collectors.toList());
	}

	@Override
	public Integer getActiveSeller() {
		// TODO Auto-generated method stub
		Optional<Seller> sellerOpt = this.sellerServiceRepo.findByIsActive(Boolean.TRUE);
		if (sellerOpt.isPresent()) {
			return sellerOpt.get().getId();
		}
		return null;
	}

	@Override
	public Page<Product> getPopularProducts(Integer sellerId, Integer pageNumber, Integer productCount) {
		// TODO Auto-generated method stub
		Sort sort = Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("reviewCount"));
		Pageable pageable = PageRequest.of(pageNumber, productCount, sort);
//		Page<com.orbit.product_service.model.Product> productsPageModel = this.productServiceRepo
//				.findBySellerId(sellerId, pageable);
		Page<com.orbit.product_service.model.Product> productsPageModel = this.productServiceRepo.findAll(pageable);
		Page<Product> productsDto = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		return productsDto;
	}

	@Override
	public ListResponse getCategories(Integer sellerId) {
		try {
			List<String> categoryNames = categoryServiceRepo.findAll().stream().map(Category::getName)
					.collect(Collectors.toList());
			return new ListResponse(categoryNames.isEmpty() ? Boolean.FALSE : Boolean.TRUE, categoryNames);
		} catch (Exception ex) {
			return new ListResponse(Boolean.FALSE, null);
		}
	}

	@Override
	public ListResponse getSellers() {
		// TODO Auto-generated method stub
		try {
			List<String> categoryNames = sellerServiceRepo.findAllByIsActive(Boolean.TRUE).stream().map(Seller::getName)
					.collect(Collectors.toList());
			return new ListResponse(categoryNames.isEmpty() ? Boolean.FALSE : Boolean.TRUE, categoryNames);
		} catch (Exception ex) {
			return new ListResponse(Boolean.FALSE, null);
		}
	}

	@Override
	public ProductsResponse getSimilarProducts(Integer sellerId, String productId) {
		// TODO Auto-generated method stub
		Optional<com.orbit.product_service.model.Product> productModel = this.productServiceRepo
				.findById(productId.toString());
		if ((productModel.isPresent())) {
			List<com.orbit.product_service.model.Product> similarProductsModel = this.productServiceRepo
					.findSimilarProducts(sellerId, productModel.get().getCategory().getId(), PageRequest.of(0, 12));
			List<Product> similarProducts = similarProductsModel.stream()
					.map(productServiceMapper::mapProductModelToView).collect(Collectors.toList());
			return new ProductsResponse(Boolean.TRUE, similarProducts);
		}
		return null;
	}

	@Async
	public CompletableFuture<Map<String, Float>> rankProductsByQuery(String prompt, String indexName) {
		Map<String, Float> rankMap = llmClient.rankProducts(prompt, indexName);
		return CompletableFuture.completedFuture(rankMap);
	}

	@Override
	public Page<Product> getFilteredProducts(String productName, String brand, String category, String subCategory,
			Integer sellerId, String sortBy, String sortDirection, Integer pageNumber, Integer productCount) {
		// TODO Auto-generated method stub
		Sort sort = Sort.unsorted();
		switch (sortBy) {
		case "price":
			sort = Sort.by(sortBy == "ASC" ? Sort.Direction.ASC : Sort.Direction.DESC, "finalPrice");
			break;
		case "rating":
			sort = Sort.by(sortBy == "ASC" ? Sort.Direction.ASC : Sort.Direction.DESC, "rating");
			break;
		default:
			break;
		}

		Pageable pageable = PageRequest.of(pageNumber, productCount, sort);

		Page<com.orbit.product_service.model.Product> productsPageModel = null;
		if (subCategory != null && brand != null && productName != null) {
			productsPageModel = productServiceRepo
					.findByCategoryNameAndSubCategoryNameAndBrandContainingIgnoreCaseAndProductNameContainingIgnoreCase(
							category, subCategory, brand, productName, pageable);
			if (!productsPageModel.isEmpty())
				return productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		}

		if (subCategory != null && productName != null) {
			productsPageModel = productServiceRepo
					.findByCategoryNameAndSubCategoryNameAndProductNameContainingIgnoreCase(category, subCategory,
							productName, pageable);
			if (!productsPageModel.isEmpty())
				return productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		}

		if (productName != null) {
			productsPageModel = productServiceRepo.findByCategoryNameAndProductNameContainingIgnoreCase(category,
					productName, pageable);
			if (!productsPageModel.isEmpty())
				return productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		}

		if (subCategory != null && brand != null) {
			productsPageModel = productServiceRepo.findByCategoryNameAndSubCategoryNameAndBrandContainingIgnoreCase(
					category, subCategory, brand, pageable);
			if (!productsPageModel.isEmpty())
				return productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		}

		if (brand != null) {
			productsPageModel = productServiceRepo.findByCategoryNameAndBrandContainingIgnoreCase(category, brand,
					pageable);
			if (!productsPageModel.isEmpty())
				return productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		}

		if (subCategory != null) {
			productsPageModel = productServiceRepo.findByCategoryNameAndSubCategoryName(category, subCategory,
					pageable);
			if (!productsPageModel.isEmpty())
				return productsPageModel.map(this.productServiceMapper::mapProductModelToView);
		}
		productsPageModel = productServiceRepo.findByCategoryName(category, pageable);
		return productsPageModel.map(this.productServiceMapper::mapProductModelToView);
	}

	public Page<Product> rankProducts(Map<String, Float> rankMap, Page<Product> productsResponse) {

		Map<Long, Integer> productIdx = new HashMap<>();

		List<Product> contentList = productsResponse.getContent();

		for (int idx = 0; idx < contentList.size(); idx++) {
			Product product = contentList.get(idx);
			productIdx.put(Long.parseLong(product.getId()), idx); // if id is String
			// or productIdx.put(product.getId(), idx); if id is already int
		}

		int count = 0;

		for (Map.Entry<String, Float> entry : rankMap.entrySet()) {
			String pid = entry.getKey();
			Float distance = entry.getValue();

			long pidLong = Long.parseLong(pid);

			if (productIdx.containsKey(pidLong)) {
				count++;

				int contentIdx = productIdx.get(pidLong);
				Product product = productsResponse.getContent().get(contentIdx);

				product.setRelevanceScore(-distance);
			}
		}
		System.out.println("Total products updated: " + count);
		List<Product> sortedProducts = productsResponse.getContent().stream()
				.sorted(Comparator.comparingDouble(Product::getRelevanceScore).reversed()).collect(Collectors.toList());

		return new PageImpl<>(sortedProducts, productsResponse.getPageable(), productsResponse.getTotalElements());
	}

	@Override
	public Page<Product> getFilteredProductsFromPrompt(String prompt, String sortBy, String sortDirection,
			Integer pageNumber, Integer productCount) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Sort sort = Sort.unsorted();
		switch (sortBy) {
		case "price":
			sort = Sort.by(sortBy == "ASC" ? Sort.Direction.ASC : Sort.Direction.DESC, "finalPrice");
			break;
		case "rating":
			sort = Sort.by(sortBy == "ASC" ? Sort.Direction.ASC : Sort.Direction.DESC, "rating");
			break;
		default:
			break;
		}

		Pageable pageable = PageRequest.of(pageNumber, productCount, sort);

		Page<com.orbit.product_service.model.Product> productsPageModel = null;

		LLMResponse response = llmClient.extractDetailsFromPrompt(prompt);

		System.out.println("LLM response: " + response.toString());

		CompletableFuture<Map<String, Float>> rankApiPromise = rankProductsByQuery(prompt,
				response.getVectorSearchIndex());

		String subCategory = response.getSubCategory();
		String brand = "NaN".equals(response.getBrand()) ? null : response.getBrand();
		String productName = response.getProductName();
		String category = response.getCategory();
		Page<Product> productsFiltered;
		System.out.println("Brand: " + brand);
		System.out.println(response.getBrand() == "NaN");
		while (true) {
			if (subCategory != null && brand != null && productName != null) {
				System.out.println("Fetching with productName, brand, subCategory");
				productsPageModel = productServiceRepo
						.findByCategoryNameAndSubCategoryNameAndBrandContainingIgnoreCaseAndProductNameContainingIgnoreCase(
								category, subCategory, brand, productName, pageable);
				if (!productsPageModel.isEmpty()) {
					productsFiltered = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
					break;
				}
			}

			if (subCategory != null && productName != null) {
				System.out.println("Fetching with productName, subCategory");
				productsPageModel = productServiceRepo
						.findByCategoryNameAndSubCategoryNameAndProductNameContainingIgnoreCase(category, subCategory,
								productName, pageable);
				if (!productsPageModel.isEmpty()) {
					productsFiltered = productsPageModel.map(this.productServiceMapper::mapProductModelToView);

					break;
				}
			}

			if (productName != null) {
				System.out.println("Fetching with productName");
				productsPageModel = productServiceRepo.findByCategoryNameAndProductNameContainingIgnoreCase(category,
						productName, pageable);
				if (!productsPageModel.isEmpty()) {
					productsFiltered = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
					break;
				}
			}

			if (subCategory != null && brand != null) {
				System.out.println("Fetching with brand, subCategory");
				productsPageModel = productServiceRepo.findByCategoryNameAndSubCategoryNameAndBrandContainingIgnoreCase(
						category, subCategory, brand, pageable);
				if (!productsPageModel.isEmpty())

				{
					productsFiltered = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
					break;
				}
			}
			
			if (subCategory != null) {
				System.out.println("Fetching with subCategory");
				productsPageModel = productServiceRepo.findBySubCategoryName(subCategory, pageable);
				if (!productsPageModel.isEmpty()) {
					productsFiltered = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
					break;
				}
			}

			if (brand != null) {
				System.out.println("Fetching with brand");
				productsPageModel = productServiceRepo.findByCategoryNameAndBrandContainingIgnoreCase(category, brand,
						pageable);
				if (!productsPageModel.isEmpty()) {
					productsFiltered = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
					break;
				}
			}

			
			System.out.println("Default");
			productsPageModel = productServiceRepo.findByCategoryName(category, pageable);
			productsFiltered = productsPageModel.map(this.productServiceMapper::mapProductModelToView);
			break;
		}
		Map<String, Float> productsResponse = rankApiPromise.join();
		return rankProducts(productsResponse, productsFiltered);

	}

}
