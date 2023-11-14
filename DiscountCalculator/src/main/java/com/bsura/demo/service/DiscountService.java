/**
 * 
 */
package com.bsura.demo.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.bsura.demo.data.model.AppliedDiscount;
import com.bsura.demo.data.model.Discount;
import com.bsura.demo.data.repositories.DiscountRepository;
import com.bsura.demo.exception.ResourceAlreadyExistsException;
import com.bsura.demo.rest.model.Cart;
import com.bsura.demo.rest.model.CartItem;
import com.bsura.demo.rest.model.DiscountDto;

/**
 * 
 */
@Service
public class DiscountService {

	private static final String NO_DISCOUNT_ID = "NoDiscount";
	
	@Autowired
	private DiscountRepository repository;
	
	/**
	 * Adds the discount to repository. 
	 * If discountId already exists in repository, 
	 * then it throws ResourceAlreadyExists runtime exception.
	 * 
	 * @param discountDto to be saved
	 * @return Discount discount
	 */
	public DiscountDto addDiscount(@NonNull final DiscountDto discountDto) {
		
		if(repository.findById(discountDto.getId()).isPresent()) {
			throw new ResourceAlreadyExistsException();
		}

		Discount savedDiscount = repository.save(new Discount(discountDto));
		return new DiscountDto(savedDiscount);
	}
	
	/**
	 * Removes the discount with given discountId. 
	 * If discountId does not exist, then it simply returns
	 * 
	 * @param discountId
	 */
	public void removeDiscount(@NonNull final String discountId) {
		repository.deleteById(discountId);
		
	}
	
	/**
	 * Method applies the discount with greatest dollar value for the given cart.
	 * If no discount applies, then the method will return a response with discountId NO_DISCOUNT_ID 
	 * and percentage discount of zero.
	 * 
	 * @param cart
	 * @return return discountId and cartTotal after applying discount.
	 */
	public AppliedDiscount getBestDiscount(@NonNull final Cart cart) {

		

		BigDecimal totalWithoutDiscount = getTotalForCartItems(cart.getCartItems());
		
		/*
		 * For simplicity we are getting all available discounts from the repository. If the number of
		 * discounts is too great, we should query for the results that match ANY of the below conditions
		 * 
		 * 1. discountType == TOTAL_COST && totalCost < maxItemCost
		 * 2. discountType == ITEM_TYPE && itemType IN getItemTypesSet(cart)
		 * 3. discountType == QUANTITY && itemId in getAllItemIds(cart) && quantity <= getMaxItemQuantity(cart)
		 */
		List<Discount> allDiscounts = repository.findAll();
		
		Optional<AppliedDiscount> appliedDiscount = allDiscounts.stream()
				.map(discount -> new AppliedDiscount(discount.getId(), getCartTotal(cart, discount)))
				.min(Comparator.comparing(AppliedDiscount::getTotalCostAfterDiscount));
		
		return appliedDiscount
			.filter(t -> t.getTotalCostAfterDiscount().compareTo(totalWithoutDiscount) < 0)
			.orElse(new AppliedDiscount(NO_DISCOUNT_ID, totalWithoutDiscount));
				

	}
	
	/**
	 * Calculates cartTotal without any discount.
	 * 
	 * @param cartItems
	 * @return
	 */
	private BigDecimal getTotalForCartItems(List<CartItem> cartItems) {
		return 	cartItems.stream()
				.map(DiscountService::getCartItemTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	/**
	 * Calculates total dollar value of the cart after applying the given discount
	 * 
	 * @param cart
	 * @param discount
	 * @return total value of cart after discount is applied
	 */
	private BigDecimal getCartTotal(final Cart cart, final Discount discount) {

		switch (discount.getDiscountType()) {
			case ITEM_COST:
				return getTotalForItemCostDiscount(cart, discount);
			case ITEM_TYPE:
				return getTotalForItemTypeDiscount(cart, discount);
			case QUANTITY:
				return getTotalForQuantityDiscount(cart, discount);
			default:
				throw new RuntimeException("Unknown discountType " + discount.getDiscountType());
				
		}
	}

	/**
	 * Applies the discountPercentage to all items if cost of item is strictly greater than the 
	 * itemCost configured for the discount.
	 * 
	 * @param cart
	 * @param discount
	 * @return cartTotal after applying discount
	 */
	private BigDecimal getTotalForItemCostDiscount(final Cart cart, final Discount discount) {

		return cart.getCartItems().stream()
				.filter(cartItem -> cartItem.getItem().getCost().compareTo(discount.getItemCost()) > 0)
				.map(cartItem -> applyDiscount(cartItem, discount.getDiscountPercentage()))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.add(
					cart.getCartItems().stream()
					.filter(cartItem -> cartItem.getItem().getCost().compareTo(discount.getItemCost()) <= 0)
					.map(DiscountService::getCartItemTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add)
					);
	}
	
	/**
	 * Applies percentage discount to all cartItems that match the itemType and no discount to others.
	 * 
	 * @param cart
	 * @param discount
	 * @return cartTotal after applying discount
	 */
	private BigDecimal getTotalForItemTypeDiscount(final Cart cart, final Discount discount) {

		return cart.getCartItems().stream()
				.filter(cartItem -> discount.getItemType() == cartItem.getItem().getItemType())
				.map(cartItem -> applyDiscount(cartItem, discount.getDiscountPercentage()))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.add(
					cart.getCartItems().stream()
					.filter(cartItem -> discount.getItemType() != cartItem.getItem().getItemType())
					.map(DiscountService::getCartItemTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add)
				);

	}
	
	/**
	 * Applies percentage discount to all cartItems with matching itemId and quantity greater than or equal to quantity.
	 * 
	 * @param cart
	 * @param discount
	 * @return cartTotal after applying discount
	 */
	private BigDecimal getTotalForQuantityDiscount(final Cart cart, final Discount discount) {

		return cart.getCartItems().stream()
				.filter(cartItem -> cartItem.getItem().getId().equals(discount.getItemId()) && cartItem.getQuantity() >= discount.getQuantity())
				.map(cartItem -> applyDiscount(cartItem, discount.getDiscountPercentage()))
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.add(
					cart.getCartItems().stream()
					.filter(cartItem -> !(cartItem.getItem().getId().equals(discount.getItemId()) && cartItem.getQuantity() >= discount.getQuantity()))
					.map(DiscountService::getCartItemTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add)
				);
	}
	
	/**
	 * Applies percentage discount to the cartItem and returns total cartItem value after discount.
	 * 
	 * @param cartItem
	 * @param discountPercentage
	 * @return cartItem total after apply discount
	 */
	private BigDecimal applyDiscount(final CartItem cartItem, final BigDecimal discountPercentage) {
		
		return cartItem.getItem().getCost()
				.multiply(BigDecimal.valueOf(cartItem.getQuantity())) 
				.multiply(BigDecimal.valueOf(100).subtract(discountPercentage).divide(BigDecimal.valueOf(100)));
	}
	
	private static BigDecimal getCartItemTotal(final CartItem cartItem) {
		return cartItem.getItem().getCost().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
	}

		
	
}
