package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.OrderItemDto;
import edu.ncsu.csc326.wolfcafe.dto.PlaceOrderDto;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;

import java.util.Arrays;

/**
 * Unit tests for OrderServiceImpl covering UC3, UC4, UC5 scenarios.
 */
@SpringBootTest
public class OrderServiceTest {

    /** Reference to OrderService */
    @Autowired
    private OrderService orderService;

    /** Reference to RecipeRepository for test data setup */
    @Autowired
    private RecipeRepository recipeRepository;

    /** Reference to OrderRepository for cleanup */
    @Autowired
    private OrderRepository orderRepository;

    /** Test customer username */
    private static final String CUSTOMER = "customer1";
    /** Test customer username */
    private static final String CUSTOMER2 = "customer2";

    /** Saved Coffee recipe id */
    private Long coffeeId;

    /** Saved Latte recipe id */
    private Long latteId;

    /**
     * Creates two recipes before each test and clears any existing orders.
     */
    @BeforeEach
    public void setUp() {
        orderRepository.deleteAll();
        recipeRepository.deleteAll();

        Recipe coffee = new Recipe("Coffee", 3.00);  // $3.00
        Recipe latte  = new Recipe("Latte",  4.50);  // $4.50

        coffeeId = recipeRepository.save(coffee).getId();
        latteId  = recipeRepository.save(latte).getId();
    }
    
    /** Places a simple one-Coffee order for CUSTOMER with 20% tip. */
    private OrderDto placeSimpleOrder() {
        List<OrderItemDto> items = List.of(new OrderItemDto(coffeeId, null, 0, 1));
        return orderService.placeOrder(CUSTOMER, new PlaceOrderDto(items, 20.0));
    }

    /**
     * Tests placing an order with one Coffee and two Lattes at 20% tip.
     * Verifies subtotal, tax, tip, and total are computed correctly.
     */
    @Test
    @Transactional
    public void testPlaceOrderMultipleItems() {
        // 1 Coffee ($3.00) + 2 Latte ($4.50 each) = $12.00 subtotal
        List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto(coffeeId, null, 0, 1),
                new OrderItemDto(latteId,  null, 0, 2)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 20.0);

        OrderDto order = orderService.placeOrder(CUSTOMER, placeOrderDto);

        assertAll("Order totals",
                () -> assertNotNull(order.getId()),
                () -> assertEquals(CUSTOMER, order.getCustomerUsername()),
                () -> assertEquals(OrderStatus.PENDING, order.getStatus()),
                () -> assertEquals(12.0, order.getSubtotal(), 0.001),
                () -> assertEquals(.24,  order.getTaxAmount(),  0.001),  // 2% of 12.00
                () -> assertEquals(2.4,  order.getTipAmount(),  0.001),  // 20% of 12.00
                () -> assertEquals(14.64, order.getTotal(),      0.001),  // 12 + 0.24 + 2.40
                () -> assertEquals(2,     order.getItems().size())
        );
    }

    /**
     * Tests that the order items contain correct recipe names and prices.
     */
    @Test
    @Transactional
    public void testPlaceOrderItemDetails() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);

        OrderDto order = orderService.placeOrder(CUSTOMER, placeOrderDto);

        OrderItemDto item = order.getItems().get(0);
        assertAll("Order item details",
                () -> assertEquals("Coffee", item.getRecipeName()),
                () -> assertEquals(3.0,      item.getPriceEach()),
                () -> assertEquals(1,        item.getQuantity())
        );
    }

    /**
     * Tests placing an order with a 15% tip.
     */
    @Test
    @Transactional
    public void testPlaceOrderFifteenPercentTip() {
        // 1 Coffee = $3.00 subtotal
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);

        OrderDto order = orderService.placeOrder(CUSTOMER, placeOrderDto);

        assertAll("15% tip calculation",
                () -> assertEquals(3.0, order.getSubtotal(),  0.001),
                () -> assertEquals(.06, order.getTaxAmount(), 0.001),
                () -> assertEquals(.45, order.getTipAmount(), 0.001),
                () -> assertEquals(3.51, order.getTotal(),     0.001)
        );
    }

    /**
     * Tests placing an order with a 25% tip.
     */
    @Test
    @Transactional
    public void testPlaceOrderTwentyFivePercentTip() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 25.0);

        OrderDto order = orderService.placeOrder(CUSTOMER, placeOrderDto);

        assertAll("25% tip calculation",
                () -> assertEquals(3.0, order.getSubtotal(),  0.001),
                () -> assertEquals(.06, order.getTaxAmount(), 0.001),
                () -> assertEquals(.75, order.getTipAmount(), 0.001),  // 25% of 3.00
                () -> assertEquals(3.81, order.getTotal(),     0.001)
        );
    }

    /**
     * Tests placing an order with a custom tip (e.g., 18%).
     */
    @Test
    @Transactional
    public void testPlaceOrderCustomTip() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 18.0);

        OrderDto order = orderService.placeOrder(CUSTOMER, placeOrderDto);

        assertAll("Custom 18% tip",
                () -> assertEquals(3.0, order.getSubtotal(),  0.001),
                () -> assertEquals(.06, order.getTaxAmount(), 0.001),
                () -> assertEquals(.54, order.getTipAmount(), 0.001),  // 18% of 3.00
                () -> assertEquals(3.6, order.getTotal(),     0.001)
        );
    }

    /**
     * Tests that when only one item is submitted the totals reflect just that item.
     * (Item removal happens on the frontend before submission; here we verify
     * the backend correctly handles orders with a subset of available items.)
     */
    @Test
    @Transactional
    public void testPlaceOrderSingleItemAfterRemoval() {
        // Customer added Mocha and Tea, then removed Tea — submits only Coffee
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);

        OrderDto order = orderService.placeOrder(CUSTOMER, placeOrderDto);

        assertAll("Single item order after removal",
                () -> assertEquals(1, order.getItems().size()),
                () -> assertEquals("Coffee", order.getItems().get(0).getRecipeName()),
                () -> assertEquals(OrderStatus.PENDING, order.getStatus())
        );
    }

    /**
     * Tests that placing an order with no items throws WolfCafeAPIException.
     * Satisfies ST-UC3-03.
     */
    @Test
    public void testPlaceEmptyOrderThrowsException() {
        PlaceOrderDto emptyOrder = new PlaceOrderDto(List.of(), 15.0);
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.placeOrder(CUSTOMER, emptyOrder));
    }

    /**
     * Tests that placing an order with a null items list throws WolfCafeAPIException.
     */
    @Test
    public void testPlaceNullItemsOrderThrowsException() {
        PlaceOrderDto nullItems = new PlaceOrderDto(null, 15.0);
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.placeOrder(CUSTOMER, nullItems));
    }

    /**
     * Tests that placing an order with a negative tip throws WolfCafeAPIException.
     */
    @Test
    public void testNegativeTipThrowsException() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto badTip = new PlaceOrderDto(items, -5.0);
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.placeOrder(CUSTOMER, badTip));
    }

    /**
     * Tests that placing an order with a non-existent recipe throws ResourceNotFoundException.
     */
    @Test
    public void testPlaceOrderInvalidRecipeThrowsException() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(-999L, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(CUSTOMER, placeOrderDto));
    }

    /**
     * Tests that placing an order with quantity 0 throws WolfCafeAPIException.
     */
    @Test
    public void testPlaceOrderZeroQuantityThrowsException() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 0)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.placeOrder(CUSTOMER, placeOrderDto));
    }

    /**
     * Tests that getOrdersForCustomer returns only orders for that customer.
     */
    @Test
    @Transactional
    public void testGetOrdersForCustomer() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        orderService.placeOrder(CUSTOMER, new PlaceOrderDto(items, 20.0));
        orderService.placeOrder("other_customer", new PlaceOrderDto(items, 15.0));

        List<OrderDto> myOrders = orderService.getOrdersForCustomer(CUSTOMER);
        assertEquals(1, myOrders.size());
        assertEquals(CUSTOMER, myOrders.get(0).getCustomerUsername());
    }

    /**
     * Tests that getOrderById returns the correct order.
     */
    @Test
    @Transactional
    public void testGetOrderById() {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 2)
        );
        OrderDto placed = orderService.placeOrder(CUSTOMER, new PlaceOrderDto(items, 20.0));

        OrderDto fetched = orderService.getOrderById(placed.getId());
        assertEquals(placed.getId(), fetched.getId());
        assertEquals(CUSTOMER, fetched.getCustomerUsername());
    }

    /**
     * Tests that getOrderById with a non-existent id throws ResourceNotFoundException.
     */
    @Test
    public void testGetOrderByIdNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(-999L));
    }
    
    /**
     * ST-UC4-02: Staff fulfills a PENDING order successfully.
     */
    @Test
    @Transactional
    public void testFulfillOrder() {
        OrderDto placed = placeSimpleOrder();
        assertEquals(OrderStatus.PENDING, placed.getStatus());
 
        OrderDto fulfilled = orderService.fulfillOrder(placed.getId());
 
        assertEquals(OrderStatus.FULFILLED, fulfilled.getStatus());
        assertEquals(placed.getId(), fulfilled.getId());
    }
 
    /**
     * ST-UC4-03: Staff attempts to fulfill an already-fulfilled order.
     * Expects WolfCafeAPIException with a 400 status.
     */
    @Test
    @Transactional
    public void testFulfillAlreadyFulfilledOrderThrows() {
        OrderDto placed = placeSimpleOrder();
        orderService.fulfillOrder(placed.getId());
 
        // Second fulfill attempt must throw
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.fulfillOrder(placed.getId()));
    }
 
    /**
     * Fulfilling a non-existent order throws ResourceNotFoundException.
     */
    @Test
    public void testFulfillNonExistentOrderThrows() {
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.fulfillOrder(-999L));
    }
 
    /**
     * ST-UC4-01: Staff can retrieve all orders.
     */
    @Test
    @Transactional
    public void testGetAllOrders() {
        placeSimpleOrder();
        List<OrderItemDto> items = List.of(new OrderItemDto(latteId, null, 0, 1));
        orderService.placeOrder(CUSTOMER2, new PlaceOrderDto(items, 15.0));
 
        List<OrderDto> all = orderService.getAllOrders();
        assertEquals(2, all.size());
    }
    
    /**
     * ST-UC5-01: Customer successfully picks up a FULFILLED order.
     */
    @Test
    @Transactional
    public void testConfirmPickup() {
        OrderDto placed    = placeSimpleOrder();
        OrderDto fulfilled = orderService.fulfillOrder(placed.getId());
        assertEquals(OrderStatus.FULFILLED, fulfilled.getStatus());
 
        OrderDto pickedUp = orderService.confirmPickup(fulfilled.getId(), CUSTOMER);
        assertEquals(OrderStatus.PICKED_UP, pickedUp.getStatus());
    }
 
    /**
     * ST-UC5-02: Customer attempts pickup on a PENDING (not yet fulfilled) order.
     * Expects WolfCafeAPIException.
     */
    @Test
    @Transactional
    public void testConfirmPickupNotFulfilledThrows() {
        OrderDto placed = placeSimpleOrder();
        assertEquals(OrderStatus.PENDING, placed.getStatus());
 
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.confirmPickup(placed.getId(), CUSTOMER));
    }
 
    /**
     * ST-UC5-03 (ownership): A different customer cannot pick up another
     * customer's order. Expects WolfCafeAPIException with 403.
     */
    @Test
    @Transactional
    public void testConfirmPickupWrongCustomerThrows() {
        OrderDto placed = placeSimpleOrder();
        orderService.fulfillOrder(placed.getId());
 
        // CUSTOMER2 tries to pick it up — must be rejected
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.confirmPickup(placed.getId(), CUSTOMER2));
    }
 
    /**
     * Confirming pickup of a non-existent order throws ResourceNotFoundException.
     */
    @Test
    public void testConfirmPickupNonExistentOrderThrows() {
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.confirmPickup(-999L, CUSTOMER));
    }
 
    /**
     * A PICKED_UP order cannot be picked up again.
     */
    @Test
    @Transactional
    public void testConfirmPickupAlreadyPickedUpThrows() {
        OrderDto placed = placeSimpleOrder();
        orderService.fulfillOrder(placed.getId());
        orderService.confirmPickup(placed.getId(), CUSTOMER);
 
        // Second pickup attempt must throw
        assertThrows(WolfCafeAPIException.class,
                () -> orderService.confirmPickup(placed.getId(), CUSTOMER));
    }
}
