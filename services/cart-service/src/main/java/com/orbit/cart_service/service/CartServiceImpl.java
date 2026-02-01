package com.orbit.cart_service.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orbit.cart_service.dao.CartServiceRepo;
import com.orbit.cart_service.dao.ItemServiceRepo;
import com.orbit.cart_service.dto.Cart;
import com.orbit.cart_service.dto.PricingSummary;
import com.orbit.cart_service.dto.Product;
import com.orbit.cart_service.dto.Response;
import com.orbit.cart_service.enums.Status;
import com.orbit.cart_service.mapper.CartServiceMapper;
import com.orbit.cart_service.model.Item;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartServiceInterface {

	private final CartServiceRepo cartServiceRepo;
	private final ItemServiceRepo itemServiceRepo;
	private final CartServiceMapper cartServiceMapper;
	private final OrderClient orderClient;
	private final ProductClient productClient;

	@Autowired
	public CartServiceImpl(CartServiceRepo cartServiceRepo, ItemServiceRepo itemServiceRepo,
			CartServiceMapper cartServiceMapper, OrderClient orderClient, ProductClient productClient) {
		// TODO Auto-generated constructor stub
		this.cartServiceRepo = cartServiceRepo;
		this.itemServiceRepo = itemServiceRepo;
		this.cartServiceMapper = cartServiceMapper;
		this.orderClient = orderClient;
		this.productClient = productClient;

	}

	@Override
	@Transactional
	public Response addToCart(Cart cart, Integer userId) {
		// TODO Auto-generated method stub
		Optional<com.orbit.cart_service.model.Cart> optCart = cartServiceRepo.findByUserIdAndStatus(userId,
				Status.ACTIVE);
		com.orbit.cart_service.model.Cart cartModel = null;
		if (optCart.isEmpty()) {
			cartModel = cartServiceMapper.mapCartDtoToModel(cart, userId);
		} else {
			cartModel = optCart.get();
			Map<String, Integer> quantityMap = new HashMap<String, Integer>();
			Map<String, Double> priceMap = new HashMap<String, Double>();
			for (com.orbit.cart_service.dto.Item item : cart.getItems()) {
				quantityMap.put(item.getId(), item.getQuantity());
				priceMap.put(item.getId(), item.getPrice());
			}
			for (Item item : cartModel.getItems()) {
				if (quantityMap.containsKey(item.getProductId())) {
					item.setQuantity(quantityMap.get(item.getProductId()));
					item.setPrice(priceMap.get(item.getProductId()));
				}
			}
			cartModel.setShippingCost(cart.getShippingCost());
			cartModel.setTax(cart.getTax());
			cartModel.setItemsTotalCost(cart.getItemsTotalCost());
		}
		cartServiceRepo.save(cartModel);
		optCart = cartServiceRepo.findById(cartModel.getId());
		if (optCart.isPresent()) {
			return new Response(Boolean.TRUE, "Items added to cart!", null);
		}
		return new Response(Boolean.FALSE, "Something went wrong will adding items to cart!", null);
	}

	@Override
	@Transactional
	public Response getCart(Integer userId) {
		// TODO Auto-generated method stub
		Optional<com.orbit.cart_service.model.Cart> cartOpt = cartServiceRepo.findByUserIdAndStatus(userId,
				Status.ACTIVE);
		if (cartOpt.isPresent()) {
			return new Response(Boolean.TRUE, "Cart retrieved successfully!", cartOpt.get().getItems().stream()
					.map(cartServiceMapper::mapItemModelToDto).collect(Collectors.toList()));
		}
		return new Response(Boolean.FALSE, "Cart empty!", null);
	}

	@Override
	@Transactional
	public Response proceedToBuy(Integer userId) {
		// TODO Auto-generated method stub
		try {
			Optional<com.orbit.cart_service.model.Cart> optCart = cartServiceRepo.findByUserIdAndStatus(userId,
					Status.ACTIVE);
			if (optCart.isEmpty()) {
				return new Response(Boolean.FALSE, "Cart empty!", null);
			} else {
				Cart cart = cartServiceMapper.mapCartModelToDto(optCart.get());
				List<String> productIds = cart.getItems().stream().map(com.orbit.cart_service.dto.Item::getId)
						.collect(Collectors.toList());
				List<Product> products = productClient.getProductsById(productIds);
				System.out.println("Products: "+products.get(0).toString());
				Map<String, String> titlesMap = new HashMap<String, String>();
				Map<String, String> urlsMap = new HashMap<String, String>();
				System.out.print(urlsMap.toString());
				for (Product product : products) {
					titlesMap.put(product.getId(), product.getProductName());
					urlsMap.put(product.getId(), product.getMainImage());
				}
				cart.getItems().stream().forEach(item -> {
					item.setName(titlesMap.getOrDefault(item.getId(), ""));
					item.setImgUrl(urlsMap.getOrDefault(item.getId(), ""));
				});

				String orderId = orderClient.proceedToBuy(cart);
				if (!orderId.isBlank()) {
					cartServiceRepo.updateCartStatus(userId, Status.INACTIVE);
					return new Response(Boolean.TRUE, "Order Placed Sucessfully!", orderId);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Exception: " + e.getLocalizedMessage());
			return new Response(Boolean.FALSE, "Something went wrong will placing order", null);
		}
		return new Response(Boolean.FALSE, "Something went wrong will placing order", null);
	}

	@Override
	@Transactional
	public Response proceedToCheckout(Cart cart, Integer userId) {
		Map<String, Integer> itemQuantityMap = cart.getItems().stream().collect(
				Collectors.toMap(com.orbit.cart_service.dto.Item::getId, com.orbit.cart_service.dto.Item::getQuantity));
		List<String> productIds = itemQuantityMap.keySet().stream().toList();
		List<Product> products = productClient.getProductsById(productIds);
		System.out.println("products: " + products.toString());
		Map<String, Double> priceMap = products.stream().collect(Collectors.toMap(Product::getId, Product::getFinalPrice));
		
		Double totalCost = 0.0;
		// find totalCost
		totalCost = products.stream().filter(product -> (itemQuantityMap.containsKey(product.getId())))
				.mapToDouble(product -> itemQuantityMap.get(product.getId()) * product.getFinalPrice()).sum();

		// set price from products
		cart.getItems().stream().forEach(item -> item.setPrice(priceMap.getOrDefault(item.getId(), 0.0)));

		cart.setItemsTotalCost(Math.round(totalCost * 100.0) / 100.0);
		cart.setTax(Math.round(0.09 * totalCost * 100.0) / 100.0);
		cart.setShippingCost(0.00);
		// for orderid creation
		cart.setSellerId(products.get(0).getSeller().toString());
		addToCart(cart, userId);
		return new Response(Boolean.TRUE, "Items added to the Cart!", null);
	}

	@Override
	public Response fetchPricing(Integer userId) {
		// TODO Auto-generated method stub
		Optional<com.orbit.cart_service.model.Cart> cartOpt = cartServiceRepo.findByUserIdAndStatus(userId,
				Status.ACTIVE);
		if (cartOpt.isPresent()) {
			return new Response(Boolean.TRUE, "",
					new PricingSummary(cartOpt.get().getItemsTotalCost(), cartOpt.get().getShippingCost(),
							cartOpt.get().getTax(), Math.round((cartOpt.get().getItemsTotalCost()
									+ cartOpt.get().getTax() + cartOpt.get().getShippingCost()) * 100.0) / 100.0));
		}
		return new Response(Boolean.TRUE, "", null);
	}

}
