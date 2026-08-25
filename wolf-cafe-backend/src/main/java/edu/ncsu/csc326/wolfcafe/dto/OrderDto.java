package edu.ncsu.csc326.wolfcafe.dto;

import java.time.LocalDateTime;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Full order response returned by the API.
 * Contains all pricing breakdowns and current status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    /** Order id */
    private Long id;

    /** Username of the customer who placed the order */
    private String customerUsername;

    /** Line items in the order */
    private List<OrderItemDto> items;

    /** Subtotal before tax and tip */
    private double subtotal;

    /** Tax rate applied (e.g. 0.02 for 2%) */
    private double taxRate;

    /** Computed tax amount */
    private double taxAmount;

    /** Tip percentage chosen by the customer */
    private double tipPercent;

    /** Computed tip amount */
    private double tipAmount;

    /** Grand total = subtotal + taxAmount + tipAmount */
    private double total;

    /** Current status of the order */
    private OrderStatus status;

    /** Timestamp when the order was placed */
    private LocalDateTime placedAt;
}
