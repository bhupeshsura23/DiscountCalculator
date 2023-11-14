/**
 * 
 */
package com.bsura.demo.data.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bsura.demo.data.model.Discount;

/**
 * 
 */

public interface DiscountRepository extends MongoRepository<Discount, String> {
	
}
