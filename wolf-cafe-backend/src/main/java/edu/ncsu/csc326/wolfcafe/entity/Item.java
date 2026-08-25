package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an item for sale in the WolfCafe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

	/** Item id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Item name */
    @Column(nullable = false, unique = true)
    private String name;
    
    /** Item description */
    private String description;
    
    /** Item price */
    @Column(nullable = false)
    private double price;


}
