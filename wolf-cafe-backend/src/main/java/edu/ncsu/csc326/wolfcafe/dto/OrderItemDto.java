package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data transfer object for a single line item in an order. The customer sends
 * recipeId and quantity; the backend fills in recipeName and priceEach from the
 * recipe record.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    /** Id of the recipe being ordered */
    private Long   recipeId;

    /** Name of the recipe (populated by the backend on response) */
    private String recipeName;

    /** Price per unit (populated by the backend on response) */
    private double    priceEach;

    /** Number of units requested */
    private int    quantity;
}
