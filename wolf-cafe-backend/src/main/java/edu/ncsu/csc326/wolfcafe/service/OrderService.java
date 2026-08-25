package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.PlaceOrderDto;

/**
 * Service interface for customer order operations (UC3, UC4, UC5).
 */
public interface OrderService {

    /**
     * Places a new order for the given customer.
     * Validates that the order is not empty, looks up each recipe,
     * computes subtotal, tax, tip, and total, then persists the order
     * with PENDING status.
     *
     * @param customerUsername username of the authenticated customer
     * @param placeOrderDto    items and tip percent from the customer
     * @return the persisted OrderDto with all computed totals
     */
    OrderDto placeOrder(String customerUsername, PlaceOrderDto placeOrderDto);

    /**
     * Returns all orders placed by the given customer.
     *
     * @param customerUsername username of the authenticated customer
     * @return list of OrderDto for that customer
     */
    List<OrderDto> getOrdersForCustomer(String customerUsername);

    /**
     * Returns all orders in the system (staff/admin view).
     *
     * @return list of all OrderDto
     */
    List<OrderDto> getAllOrders();

    /**
     * Returns the order with the given id.
     *
     * @param orderId order id
     * @return OrderDto for the order
     */
    OrderDto getOrderById(Long orderId);
    
    //OrderDto updateOrderStatus(Long orderId);
    
    /**
     * Advances a PENDING order to FULFILLED status.
     * Throws WolfCafeAPIException if the order is not currently PENDING
     * (e.g. already fulfilled).
     *
     * @param orderId the id of the order to fulfill
     * @return updated OrderDto with FULFILLED status
     */
    OrderDto fulfillOrder(Long orderId);
    
    /**
     * Advances a FULFILLED order to PICKED_UP status.
     * Validates that the order belongs to the given customer
     * and that its current status is FULFILLED.
     *
     * @param orderId          the id of the order to pick up
     * @param customerUsername the username of the requesting customer
     * @return updated OrderDto with PICKED_UP status
     */
    OrderDto confirmPickup(Long orderId, String customerUsername);
}
