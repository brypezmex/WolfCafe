package edu.ncsu.csc326.wolfcafe.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload sent by a customer to place a new order. The customer provides the
 * list of items and their chosen tip percentage. The backend computes subtotal,
 * tax, tip amount, and total.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderDto {

    /**
     * List of items the customer wants to order. Each entry carries a recipeId
     * and quantity.
     */
    private List<OrderItemDto> items;

    /**
     * Tip percentage chosen by the customer. Standard options are 15.0, 20.0,
     * 25.0, or a custom value >= 0.
     */
    private double             tipPercent;
}
