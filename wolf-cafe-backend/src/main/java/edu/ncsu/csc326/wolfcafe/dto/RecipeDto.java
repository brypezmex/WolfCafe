package edu.ncsu.csc326.wolfcafe.dto;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;

/**
 * Used to transfer Recipe data between the client and server.
 */
public class RecipeDto {

    /** Recipe Id */
    private Long             id;

    /** Recipe name */
    private String           name;

    /** Recipe price */
    private Double           price;

    /** Ingredients for this recipe */
    private List<Ingredient> ingredients;

    /**
     * Default constructor for RecipeDto.
     */
    public RecipeDto () {
        this.ingredients = new ArrayList<>();
    }

    /**
     * Creates recipe from field values.
     *
     * @param id
     *            recipe id
     * @param name
     *            recipe name
     * @param price
     *            recipe price
     */
    public RecipeDto ( Long id, String name, Double price ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = new ArrayList<>();
    }

    /**
     * Creates recipe from field values.
     *
     * @param name
     *            recipe name
     * @param price
     *            recipe price
     */
    public RecipeDto ( String name, Double price ) {
        this.name = name;
        this.price = price;
        this.ingredients = new ArrayList<>();
    }

    /**
     * Adds an ingredient.
     *
     * @param ingredient
     *            ingredient to add
     */
    public void addIngredient ( Ingredient ingredient ) {
        if ( this.ingredients == null ) {
            this.ingredients = new ArrayList<>();
        }
        this.ingredients.add( ingredient );
    }

    /**
     * Gets ingredients.
     *
     * @return ingredients
     */
    public List<Ingredient> getIngredients () {
        if ( this.ingredients == null ) {
            this.ingredients = new ArrayList<>();
        }
        return ingredients;
    }

    /**
     * Sets ingredients.
     *
     * @param ingredients
     *            ingredients to set
     */
    public void setIngredients ( List<Ingredient> ingredients ) {
        this.ingredients = ingredients == null ? new ArrayList<>() : ingredients;
    }

    /**
     * Gets the recipe id.
     *
     * @return id
     */
    public Long getId () {
        return id;
    }

    /**
     * Sets recipe id.
     *
     * @param id
     *            id to set
     */
    public void setId ( Long id ) {
        this.id = id;
    }

    /**
     * Gets recipe name.
     *
     * @return name
     */
    public String getName () {
        return name;
    }

    /**
     * Sets recipe name.
     *
     * @param name
     *            name to set
     */
    public void setName ( String name ) {
        this.name = name;
    }

    /**
     * Gets recipe price.
     *
     * @return price
     */
    public Double getPrice () {
        return price;
    }

    /**
     * Sets recipe price.
     *
     * @param price
     *            price to set
     */
    public void setPrice ( Double price ) {
        this.price = price;
    }
}
