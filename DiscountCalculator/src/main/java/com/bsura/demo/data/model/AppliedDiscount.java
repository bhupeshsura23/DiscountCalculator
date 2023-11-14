/**
 * 
 */
package com.bsura.demo.data.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppliedDiscount {
	
	private String discountId;
	private BigDecimal totalCostAfterDiscount;

}
