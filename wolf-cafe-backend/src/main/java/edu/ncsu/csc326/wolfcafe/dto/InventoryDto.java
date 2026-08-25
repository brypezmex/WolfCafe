package edu.ncsu.csc326.wolfcafe.dto;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;

/**
 * Used to transfer Inventory data between the client and server.  
 * This class will serve as the response in the REST API.
 */
public class InventoryDto {
    
    /** id for inventory entry */
    private Long    id;
    
    private List<Ingredient> ingredients;
    
    /** 
     * Default InventoryDto constructor.
     */
    public InventoryDto() {
        this.ingredients = new ArrayList<>();
        
    }
    
    /**
     * Constructs an InventoryDto object from field values.
     * @param id inventory id
     * @param coffee amount coffee in inventory
     * @param milk amount milk in inventory
     * @param sugar amount sugar in inventory
     * @param chocolate amount chocolate in inventory
     */
    public InventoryDto(Long id) {
        super();
        this.id = id;
        this.ingredients = new ArrayList<Ingredient>();
        
    }

    /**
     * Gets the inventory id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Inventory id to set.
     * @param id the id to set
     */
    public void setId(Long id) {
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