package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.dto.PlaceOrderDto;
import edu.ncsu.csc326.wolfcafe.service.OrderService;
import lombok.AllArgsConstructor;

/**
 * REST controller for customer order operations (UC3, UC4, UC5).
 *
 * Base path: /api/orders
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/orders" )
@AllArgsConstructor
public class OrderController {

    /** Link to OrderService */
    private OrderService orderService;

    /**
     * Places a new order for the authenticated customer.
     *
     * The customer provides a list of {recipeId, quantity} pairs and a tip
     * percentage. The backend resolves each recipe, computes subtotal, tax, and
     * tip, and persists the order with PENDING status.
     *
     * @param userDetails
     *            injected from the JWT security context
     * @param placeOrderDto
     *            items and tip percent from the request body
     * @return the created OrderDto with HTTP 201 Created
     */
    @PreAuthorize ( "hasRole('CUSTOMER')" )
    @PostMapping
    public ResponseEntity<OrderDto> placeOrder ( @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PlaceOrderDto placeOrderDto ) {

        String username = userDetails.getUsername();
        OrderDto createdOrder = orderService.placeOrder( username, placeOrderDto );
        return new ResponseEntity<>( createdOrder, HttpStatus.CREATED );
    }

    /**
     * Returns all orders placed by the authenticated customer.
     *
     * @param userDetails
     *            injected from the JWT security context
     * @return list of OrderDto for the authenticated customer
     */
    @PreAuthorize ( "hasRole('CUSTOMER')" )
    @GetMapping ( "/my" )
    public ResponseEntity<List<OrderDto>> getMyOrders ( @AuthenticationPrincipal UserDetails userDetails ) {

        String username = userDetails.getUsername();
        List<OrderDto> orders = orderService.getOrdersForCustomer( username );
        return ResponseEntity.ok( orders );
    }

    /**
     * Returns all orders in the system (staff view for UC4 - View and Fulfill
     * Orders). Included here since OrderService already provides it and staff
     * need it.
     *
     * @return list of all OrderDto
     */
    @PreAuthorize ( "hasAnyRole('STAFF', 'ADMIN')" )
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders () {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok( orders );
    }

    /**
     * Returns a single order by id.
     *
     * @param id
     *            order id
     * @return the OrderDto for that order
     */
    @PreAuthorize ( "hasAnyRole('STAFF', 'ADMIN')" )
    @GetMapping ( "/{id}" )
    public ResponseEntity<OrderDto> getOrderById ( @PathVariable ( "id" ) Long id ) {
        OrderDto order = orderService.getOrderById( id );
        return ResponseEntity.ok( order );
    }

    /**
     * Fulfills a PENDING order, advancing its status to FULFILLED. Returns 400
     * if the order is not currently PENDING.
     *
     * PUT /api/orders/{id}/fulfill Role required: STAFF or ADMIN
     *
     * @param id
     *            the order id to fulfill
     * @return the updated OrderDto with FULFILLED status
     */
    @PreAuthorize ( "hasAnyRole('STAFF', 'ADMIN')" )
    @PutMapping ( "/{id}/fulfill" )
    public ResponseEntity<OrderDto> fulfillOrder ( @PathVariable ( "id" ) Long id ) {
        OrderDto updated = orderService.fulfillOrder( id );
        return ResponseEntity.ok( updated );
    }

    /**
     * Confirms pickup of a FULFILLED order, advancing its status to PICKED_UP
     * Returns 400 if the order is not yet fulfilled. Returns 403 if the order
     * does not belong to the authenticated customer Spring Security also blocks
     * non-CUSTOMER roles entirely.
     *
     * PUT /api/orders/{id}/pickup Role required: CUSTOMER
     *
     * @param id
     *            the order id to pick up
     * @param userDetails
     *            injected from JWT security context
     * @return the updated OrderDto with PICKED_UP status
     */
    @PreAuthorize ( "hasRole('CUSTOMER')" )
    @PutMapping ( "/{id}/pickup" )
    public ResponseEntity<OrderDto> confirmPickup ( @PathVariable ( "id" ) Long id,
            @AuthenticationPrincipal UserDetails userDetails ) {

        String username = userDetails.getUsername();
        OrderDto updated = orderService.confirmPickup( id, username );
        return ResponseEntity.ok( updated );
    }

    // /**
    // * Updates the status of an order.
    // * Staff/Admin can move PENDING -> FULFILLED or CANCELLED.
    // * Customer can move FULFILLED -> PICKED_UP.
    // *
    // * @param orderId the order id
    // * @param newStatus the new status
    // * @return updated OrderDto
    // */
    // @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'CUSTOMER')")
    // @PutMapping("/{orderId}/status")
    // public ResponseEntity<OrderDto> updateOrderStatus(
    // @PathVariable("orderId") Long orderId) {
    //
    // OrderDto updated = orderService.updateOrderStatus(orderId);
    // return ResponseEntity.ok(updated);
    // }
}
