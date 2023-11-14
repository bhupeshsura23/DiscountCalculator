/**
 * 
 */
package com.bsura.demo.rest.model;

import java.math.BigDecimal;
import java.util.Optional;

import com.bsura.demo.data.model.Discount;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDto {

	@NotEmpty 
	private String id;

	@NotNull 
	private DiscountType discountType;
	@NotNull 
	private BigDecimal discountPercentage;
	
	private Optional<ItemType> itemType;
	private Optional<BigDecimal> itemCost;
	private Optional<Integer> quantity;
	private Optional<String> itemId;

	public DiscountDto(Discount discount) {
		this.id = discount.getId();
		this.discountType = discount.getDiscountType();
		this.discountPercentage = discount.getDiscountPercentage();
		
		this.itemType = Optional.ofNullable(discount.getItemType());
		this.itemCost = Optional.ofNullable(discount.getItemCost());
		this.quantity = Optional.ofNullable(discount.getQuantity());
		this.itemId = Optional.ofNullable(discount.getItemId());
	}
	
	@JsonIgnore
	public boolean isValid() {

		// discountPercentage can be between zero and 100 both inclusive
		if (discountPercentage.compareTo(BigDecimal.ZERO) < 0
				|| discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
			return false;
		}
		
		//validate itemType, quantity and totalCost based
		switch (discountType) {
			case ITEM_TYPE:
				return itemType.isPresent();
			case QUANTITY:
				return quantity.isPresent() && quantity.get() >= 0 && itemId.isPresent();
			case ITEM_COST:
				return itemCost.isPresent() && itemCost.get().compareTo(BigDecimal.ZERO) >= 0;

		}
		return false;
	}
}
