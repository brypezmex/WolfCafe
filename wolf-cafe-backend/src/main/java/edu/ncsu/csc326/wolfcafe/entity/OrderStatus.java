package edu.ncsu.csc326.wolfcafe.entity;

/**
 * Represents the lifecycle status of a customer order.
 */
public enum OrderStatus {
    /** Order has been placed and is waiting to be fulfilled by staff */
    PENDING,
    /** Order has been fulfilled by staff and is ready for pickup */
    FULFILLED,
    /** Order has been picked up by the customer */
    PICKED_UP,
    /** Order was cancelled before fulfillment */
    CANCELLED
}
