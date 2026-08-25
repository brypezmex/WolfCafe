package edu.ncsu.csc326.wolfcafe.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a customer order in WolfCafe.
 * An order belongs to one customer (tracked by username) and contains
 * one or more OrderItems. Pricing fields are stored as doubles so that
 * tax and tip calculations are preserved.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    /** Order id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username of the customer who placed the order */
    @Column(nullable = false)
    private String customerUsername;

    /** Line items in this order */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    /** Subtotal before tax and tip (sum of item price * quantity) */
    @Column(nullable = false)
    private double subtotal;

    /** Tax rate applied at time of order (e.g. 0.02 for 2%) */
    @Column(nullable = false)
    private double taxRate;

    /** Computed tax amount = subtotal * taxRate */
    @Column(nullable = false)
    private double taxAmount;

    /**
     * Tip percentage chosen by the customer (e.g. 15.0, 20.0, 25.0,
     * or a custom value). Stored as a percentage, not a decimal.
     */
    @Column(nullable = false)
    private double tipPercent;

    /** Computed tip amount = subtotal * (tipPercent / 100) */
    @Column(nullable = false)
    private double tipAmount;

    /** Grand total = subtotal + taxAmount + tipAmount */
    @Column(nullable = false)
    private double total;

    /** Current status of the order */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    /** Timestamp when the order was placed */
    @Column(nullable = false)
    private LocalDateTime placedAt = LocalDateTime.now();
    
    
}
