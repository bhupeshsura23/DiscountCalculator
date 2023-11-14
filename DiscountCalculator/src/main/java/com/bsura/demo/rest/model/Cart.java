/**
 * 
 */
package com.bsura.demo.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 */
@Data
public class Cart {
	
	@NotNull
	private List<CartItem> cartItems;
	
	@JsonIgnore
	public boolean isValid() {
		if(cartItems == null || cartItems.isEmpty()) {
			return false;
		}
		
		return cartItems.stream()
				.noneMatch(cartItem -> cartItem.isValid() == false);
	}

}
