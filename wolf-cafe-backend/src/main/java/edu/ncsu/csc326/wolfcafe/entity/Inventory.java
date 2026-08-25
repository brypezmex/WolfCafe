package edu.ncsu.csc326.wolfcafe.entity;


import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

/**
 * Inventory for the coffee maker. Inventory is a Data Access Object (DAO) is tied to the database using
 * Hibernate libraries. InventoryRepository provides the methods for database CRUD operations.
 */
@Entity
public class Inventory {
    
    /** id for inventory entry */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long    id;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Ingredient> ingredients;  
    
    
    /**
     * Empty constructor for Hibernate
     */
    public Inventory () {
        this.ingredients = new ArrayList<>();
    }
    
    /**
     * Creates an Inventory with all fields
     * @param id inventory's id
     * @param coffee inventory's amount coffee
     * @param milk inventory's amount milk
     * @param sugar inventory's amount sugar
     * @param chocolate inventory's amount chocolate
     */
    public Inventory(Long id) {
        super();
        this.id = id;
        this.ingredients = new ArrayList<Ingredient>();
      
    }
    
    


    /**
     * Returns the ID of the entry in the DB
     *
     * @return long
     */
    public Long getId () {
        return id;
    }

    /**
     * Set the ID of the Inventory (Used by Hibernate)
     *
     * @param id
     *            the ID
     */
    public void setId ( final Long id ) {
        this.id = id;
    }

    public List<Ingredient> getIngredients () {
        return ingredients;
    }

    public void setIngredients ( List<Ingredient> ingredients ) {
        this.ingredients = ingredients;
    }
    
    public void addIngredient(Ingredient i) {
        this.ingredients.add( i );
    }
    
    public Ingredient getIngredientByName(String name) {
        
        for (int i = 0; i < this.ingredients.size(); i++) {
            if (this.ingredients.get( i ).getIngredientName().equals( name )) {
                return this.ingredients.get( i );
            }
        }
        
        return null;
    }
    
    public void subtractIngredient(String name, int amount) {
        getIngredientByName(name).setAmount( getIngredientByName(name).getAmount() - amount );
    }

    

}