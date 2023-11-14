/**
 * 
 */
package com.bsura.demo.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
	
	@NotNull
	private Integer quantity;
	
	@NotNull
	private Item item;

	@JsonIgnore
	public boolean isValid() {
		return quantity != null 
				&& quantity > 0
				&& item != null 
				&& item.isValid();
	}
}
