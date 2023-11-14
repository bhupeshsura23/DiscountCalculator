/**
 * 
 */
package com.bsura.demo.rest.model;

import java.math.BigDecimal;

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
@AllArgsConstructor
@NoArgsConstructor
public class Item {
	
	@NotEmpty
	private String id;
	@NotNull
	private ItemType itemType;
	@NotNull
	private BigDecimal cost;
	
	@JsonIgnore
	public boolean isValid() {
		return id != null 
				&& itemType !=null 
				&& cost != null 
				&& cost.compareTo(BigDecimal.ZERO) >= 0;
	}

}
