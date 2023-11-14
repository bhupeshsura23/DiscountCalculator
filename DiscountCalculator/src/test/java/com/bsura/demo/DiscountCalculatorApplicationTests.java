package com.bsura.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bsura.demo.data.model.AppliedDiscount;
import com.bsura.demo.data.model.Discount;
import com.bsura.demo.rest.model.Cart;
import com.bsura.demo.rest.model.CartItem;
import com.bsura.demo.rest.model.DiscountDto;
import com.bsura.demo.rest.model.DiscountType;
import com.bsura.demo.rest.model.Item;
import com.bsura.demo.rest.model.ItemType;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
class DiscountCalculatorApplicationTests {

	private static final String HOST_PREFIX = "http://localhost:";
	private static final String CREATE_ENDPOINT = "/discounts/add";
	private static final String DELETE_ENDPOINT = "/discounts/";
	private static final String BEST_DISCOUNT_ENDPOINT = "/discounts/";
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@BeforeEach
	void cleanUp() {
		mongoTemplate.getDb().drop();
	}
	
	@Test
	void contextLoads() {
		assertThat(restTemplate).isNotNull();
		assertThat(mongoTemplate).isNotNull();
	}
	
	@Test
	void testCreateDiscount() {
		DiscountDto discountDto = new DiscountDto("ABC", DiscountType.ITEM_TYPE, BigDecimal.TEN, 
				Optional.of(ItemType.CLOTHES), Optional.empty(), Optional.empty(), Optional.empty());
		
		
		DiscountDto response = addDiscount(discountDto);
		
		assertThat(response.getId()).isEqualTo("ABC");
		assertThat(response.getDiscountType()).isEqualTo(DiscountType.ITEM_TYPE);
		assertThat(response.getDiscountPercentage()).isEqualTo(BigDecimal.TEN);
		assertThat(response.getItemType().isPresent()).isTrue();
		assertThat(response.getItemType().get()).isEqualTo(ItemType.CLOTHES);
		assertThat(response.getItemCost().isPresent()).isFalse();
		assertThat(response.getQuantity().isPresent()).isFalse();
		
	}
	
	@Test
	void testCreateDuplicateDiscount() {
		DiscountDto discountDto = new DiscountDto("ABC", DiscountType.ITEM_TYPE, BigDecimal.TEN, 
				Optional.of(ItemType.CLOTHES), Optional.empty(), Optional.empty(), Optional.empty());
		
		addDiscount(discountDto);
		
		ResponseEntity<DiscountDto> responseEntity =
				restTemplate.postForEntity(HOST_PREFIX + port + CREATE_ENDPOINT, discountDto, DiscountDto.class);
		
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		
	}
	
	@Test
	void testDeleteDiscount() {
		DiscountDto discountDto = new DiscountDto("ABC", DiscountType.ITEM_TYPE, BigDecimal.TEN, 
				Optional.of(ItemType.CLOTHES), Optional.empty(), Optional.empty(), Optional.empty());
		
		addDiscount(discountDto);
		
		assertThat(mongoTemplate.findAll(Discount.class).size()).isEqualTo(1);
		
		//delete unknown id. document count remains unchanged.
		restTemplate.delete(HOST_PREFIX + port + DELETE_ENDPOINT + "XYZ");
		assertThat(mongoTemplate.findAll(Discount.class).size()).isEqualTo(1);
		
		restTemplate.delete(HOST_PREFIX + port + DELETE_ENDPOINT + "ABC");
		assertThat(mongoTemplate.findAll(Discount.class).size()).isEqualTo(0);
		
	}
	
	/**
	 * GIVEN
	 * Discount ABC exists that gives 10% off all items of type CLOTHES
	 * Discount CDE exists that gives 15% off all items over $100
	 * WHEN
	 * User submits a request to calculate the best discount for a $50 shirt(id: 123, type: CLOTHES, cost: $50)
	 * THEN
	 * The system should response with discount ABC and a total cost of $45
	 */
	@Test
	void testGetDiscountItemType() {
		setupDiscounts();
		
		Cart cart = new Cart();
		Item item = new Item("123", ItemType.CLOTHES, BigDecimal.valueOf(50));
		cart.setCartItems(List.of(new CartItem(1, item)));

		
		ResponseEntity<AppliedDiscount> responseEntity =
		restTemplate.postForEntity(HOST_PREFIX + port + BEST_DISCOUNT_ENDPOINT, cart, AppliedDiscount.class);
		
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody().getDiscountId()).isEqualTo("ABC");
		assertThat(responseEntity.getBody().getTotalCostAfterDiscount().compareTo(BigDecimal.valueOf(45))).isEqualTo(0);

		
		
	}
	
	/**
	 * GIVEN
     * Discount ABC exists that gives 10% off all items of type CLOTHES
     * Discount CDE exists that gives 15% off all items over $100
     * Discount FGH exists that gives 20% off when purchasing 2 or more of shirts with id 123
     * WHEN
	 * User submits a request to calculate the best discount for five $50 shirts(id: 123, type: CLOTHES, cost: $50)
	 * THEN
	 * The system should response with discount FGH and a total cost of $200
	 */
	@Test
	void testGetDiscountMultipleItems() {
		setupDiscounts();
		
		Cart cart = new Cart();
		Item item1 = new Item("123", ItemType.CLOTHES, BigDecimal.valueOf(50));
		Item item2 = new Item("456", ItemType.ELECTRONICS, BigDecimal.valueOf(300));
		cart.setCartItems(List.of(new CartItem(1, item1), new CartItem(1, item2)));

		
		ResponseEntity<AppliedDiscount> responseEntity =
		restTemplate.postForEntity(HOST_PREFIX + port + BEST_DISCOUNT_ENDPOINT, cart, AppliedDiscount.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		AppliedDiscount appliedDiscount = responseEntity.getBody();
		assertThat(appliedDiscount.getDiscountId()).isEqualTo("CDE");
		assertThat(appliedDiscount.getTotalCostAfterDiscount().compareTo(BigDecimal.valueOf(305)))
		.withFailMessage("Total cost after discount is expected 305 but found %s", appliedDiscount.getTotalCostAfterDiscount())
		.isEqualTo(0);
		
	}
	
	/**
	 * GIVEN
	 * Discount ABC exists that gives 10% off all items of type CLOTHES
	 * Discount CDE exists that gives 15% off all items over $100
	 * WHEN
	 * User submits a request to calculate the best discount for
	 * one $50 shirt(id: 123, type: CLOTHES, cost: $50)
	 * one $300 TV(id: 456, type: ELECTRONICS, cost: $300)
	 * THEN
	 * The system should response with discount CDE and a total cost of $305
	 */
	@Test
	void testGetDiscountTotalDiscount() {
		setupDiscounts();
		
		DiscountDto discountDto = new DiscountDto("FGH", DiscountType.QUANTITY, BigDecimal.valueOf(20), 
				Optional.empty(), Optional.empty(), Optional.of(2), Optional.of("123"));
		
		addDiscount(discountDto);
		
		Cart cart = new Cart();
		Item item1 = new Item("123", ItemType.CLOTHES, BigDecimal.valueOf(50));
		cart.setCartItems(List.of(new CartItem(5, item1)));

		
		ResponseEntity<AppliedDiscount> responseEntity2 =
		restTemplate.postForEntity(HOST_PREFIX + port + BEST_DISCOUNT_ENDPOINT, cart, AppliedDiscount.class);
		assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.OK);

		AppliedDiscount appliedDiscount = responseEntity2.getBody();
		assertThat(appliedDiscount.getDiscountId()).isEqualTo("FGH");
		assertThat(appliedDiscount.getTotalCostAfterDiscount().compareTo(BigDecimal.valueOf(200)))
		.withFailMessage("Total cost after discount is expected 200 but found %s", appliedDiscount.getTotalCostAfterDiscount())
		.isEqualTo(0);

		
	}
	
	/**
	 * creates ABC and CDE discounts
	 */
	private void setupDiscounts() {
		DiscountDto discountDto = new DiscountDto("ABC", DiscountType.ITEM_TYPE, BigDecimal.TEN, 
				Optional.of(ItemType.CLOTHES), Optional.empty(), Optional.empty(), Optional.empty());
		
		DiscountDto secondDiscountDto = new DiscountDto("CDE", DiscountType.ITEM_COST, BigDecimal.valueOf(15), 
				Optional.empty(), Optional.of(BigDecimal.valueOf(100)), Optional.empty(), Optional.empty());
		
		addDiscount(discountDto);
		addDiscount(secondDiscountDto);

		
	}
	
	/**
	 * Stores the given discountDto to repository by calling create http endpoint
	 * 
	 * @param discountDto
	 * @return
	 */
	private DiscountDto addDiscount(final DiscountDto discountDto) {
		ResponseEntity<DiscountDto> responseEntity =
		restTemplate.postForEntity(HOST_PREFIX + port + CREATE_ENDPOINT, discountDto, DiscountDto.class);
		
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		return responseEntity.getBody();
	}

}
