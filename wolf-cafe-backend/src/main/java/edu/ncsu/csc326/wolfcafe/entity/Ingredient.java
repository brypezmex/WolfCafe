package edu.ncsu.csc326.wolfcafe.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Ingredient {
    
    /** Ingredient id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String ingredientName;
    
    private Integer amount;
    
    
    public Ingredient () {
        
    }

    public Ingredient (  String ingredientName, Integer amount ) {
        this.ingredientName = ingredientName;
        this.amount = amount;
    }

    public Long getId () {
        return id;
    }

    public void setId ( Long id ) {
        this.id = id;
    }

    public String getIngredientName () {
        return this.ingredientName;
    }

    public void setIngredientName ( String ingredientName ) {
        this.ingredientName = ingredientName;
    }

    public Integer getAmount () {
        return amount;
    }

    public void setAmount ( Integer amount ) {
        this.amount = amount;
    }
    
    
    

}
