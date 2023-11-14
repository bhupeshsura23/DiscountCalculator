/**
 * 
 */
package com.bsura.demo.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bsura.demo.data.model.AppliedDiscount;
import com.bsura.demo.rest.model.Cart;
import com.bsura.demo.rest.model.DiscountDto;
import com.bsura.demo.service.DiscountService;

/**
 * 
 */
@RestController
public class DiscountsRestController implements DiscountRestOperations {

	@Autowired
	private DiscountService discountService;

	@Override
	public DiscountDto createDiscount(DiscountDto discount) {

		if (!discount.isValid()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		return discountService.addDiscount(discount);
	}

	@Override
	public void removeDiscount(String discountId) {

		discountService.removeDiscount(discountId);

	}

	@Override
	public AppliedDiscount getBestDiscount(Cart cart) {
		if (!cart.isValid()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

		return discountService.getBestDiscount(cart);
	}

}
