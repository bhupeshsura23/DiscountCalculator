/**
 * 
 */
package com.bsura.demo.data.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.bsura.demo.rest.model.DiscountDto;
import com.bsura.demo.rest.model.DiscountType;
import com.bsura.demo.rest.model.ItemType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@NoArgsConstructor
@Document(collection = "discounts")
public class Discount {

	@Id
	private String id;
	

	private DiscountType discountType;
	private BigDecimal discountPercentage;
	private ItemType itemType;
	private BigDecimal itemCost;
	private Integer quantity;
	private String itemId;

	
	public Discount(DiscountDto discountDto) {
		this.id = discountDto.getId();
		this.discountType = discountDto.getDiscountType();
		this.discountPercentage = discountDto.getDiscountPercentage();
		
		this.itemType = discountDto.getItemType().orElse(null);
		this.itemCost = discountDto.getItemCost().orElse(null);
		this.quantity = discountDto.getQuantity().orElse(null);
		this.itemId = discountDto.getItemId().orElse(null);
		
	}
	
}
