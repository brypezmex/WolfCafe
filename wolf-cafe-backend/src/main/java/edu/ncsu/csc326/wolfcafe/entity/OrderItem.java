package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single line item in a customer order. Stores a snapshot of the
 * recipe name and price at the time of ordering so that price changes to the
 * recipe do not affect historical orders.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "order_items" )
public class OrderItem {

    /** Order item id */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long   id;

    /** Id of the recipe this item refers to */
    @Column ( nullable = false )
    private Long   recipeId;

    /** Snapshot of the recipe name at time of order */
    @Column ( nullable = false )
    private String recipeName;

    /** Snapshot of the recipe price at time of order */
    @Column ( nullable = false )
    private double    priceEach;

    /** Number of units of this recipe in the order */
    @Column ( nullable = false )
    private int    quantity;
}
