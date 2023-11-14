/**
 * 
 */
package com.bsura.demo.rest.controller;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bsura.demo.data.model.AppliedDiscount;
import com.bsura.demo.rest.model.Cart;
import com.bsura.demo.rest.model.DiscountDto;

import jakarta.validation.Valid;

/**
 * 
 */
@RestController("/dummy")
@RequestMapping("/discounts")
public interface DiscountRestOperations {

	@PostMapping("/add")
	public DiscountDto createDiscount(@RequestBody @Valid final DiscountDto discount);

	@DeleteMapping("/{discountId}")
	public void removeDiscount(@PathVariable @NonNull final String discountId);

	// Can use GetMapping but request URL would look like
	// http://localhost:8080/?cartItems[quantity]=1&cartItems[item][id]=123&cartItems[item][itemType]=CLOTHES&cartItems[item][cost]=39.99
	@PostMapping("/")
	public AppliedDiscount getBestDiscount(@RequestBody final Cart cart);

}
