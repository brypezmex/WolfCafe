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
import jakarta.persistence.Table;

/**
 * Recipe for the coffee maker. Recipe is a Data Access Object (DAO) is tied to the database using
 * Hibernate libraries. RecipeRepository provides the methods for database CRUD operations.
 */
@Entity
@Table(name = "recipes")
public class Recipe {

    /** Recipe id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long    id;

    /** Recipe name */
    private String name;

    /** Recipe price */
    private Double price;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Ingredient> ingredients;

    
    /**
     * Creates a default recipe for the coffee maker.
     */
    public Recipe () {
        this.name = "";
    }
    
    /**
     * Creates a recipe from all the fields
     * @param id recipe id
     * @param name recipe name
     * @param price recipe price
     * @param coffee amount of coffee
     * @param milk amount of milk
     * @param sugar amount of sugar
     * @param chocolate amount of chocolate
     */
    public Recipe(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = new ArrayList<Ingredient>();
      
    }
    
    /**
     * Creates a recipe from all the fields
     * @param name recipe name
     * @param price recipe price
     * @param coffee amount of coffee
     * @param milk amount of milk
     * @param sugar amount of sugar
     * @param chocolate amount of chocolate
     */
    public Recipe(String name, Double price) {
        this.name = name;
        this.price = price;
        this.ingredients = new ArrayList<Ingredient>();
     
    }
    
    public void addIngredient(Ingredient ingredient) {
        ingredients.add( ingredient );
    }
    
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Get the ID of the Recipe
     *
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID of the Recipe (Used by Hibernate)
     *
     * @param id
     *            the ID
     */
    @SuppressWarnings ( "unused" )
    private void setId ( final Long id ) {
        this.id = id;
    }

    

   

    
    /**
     * Returns name of the recipe.
     *
     * @return Returns the name.
     */
    public String getName () {
        return name;
    }

    /**
     * Sets the recipe name.
     *
     * @param name
     *            The name to set.
     */
    public void setName ( final String name ) {
        this.name = name;
    }

    /**
     * Returns the price of the recipe.
     *
     * @return Returns the price.
     */
    public Double getPrice () {
        return price;
    }

    /**
     * Sets the recipe price.
     *
     * @param price
     *            The price to set.
     */
    public void setPrice ( final Double price ) {
        this.price = price;
    }

}
