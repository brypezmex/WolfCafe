package edu.ncsu.csc326.wolfcafe.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import edu.ncsu.csc326.wolfcafe.service.MakeRecipeService;

/**
 * Implementation of the MakeRecipeService interface.
 */
@Service
public class MakeRecipeServiceImpl implements MakeRecipeService {

    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private InventoryService inventoryService;

    /**
     * Removes the ingredients used to make the specified recipe. Assumes that
     * the user has checked that there are enough ingredients to make
     *
     * @param inventoryDto
     *            current inventory
     * @param recipeDto
     *            recipe to make
     * @return updated inventory
     */
    @Override
    public boolean makeRecipe(InventoryDto inventoryDto, RecipeDto recipeDto) {
        Inventory inventory = mapToInventory(inventoryDto);
        Recipe recipe = mapToRecipe(recipeDto);
 
        if (enoughIngredients(inventory, recipe)) {
            subtractFromInventory(recipe);
            return true;
        }
 
        return false;
    }

    /**
     * Returns true if there are enough ingredients to make the beverage.
     *
     * @param inventory
     *            coffee maker inventory
     * @param recipe
     *            recipe to check if there are enough ingredients
     * @return true if enough ingredients to make the beverage
     */
    private boolean enoughIngredients ( Inventory inventory, Recipe recipe ) {

//        for ( int i = 0; i < recipe.getIngredients().size(); i++ ) {
//            if ( recipe.getIngredients().get( i ).getAmount() > inventory
//                    .getIngredientByName( recipe.getIngredients().get( i ).getIngredientName() ).getAmount() ) {
//                return false;
//            }
//        }
//
//        return true;
        
        for (Ingredient recipeIngredient : recipe.getIngredients()) {
            Ingredient inventoryIngredient =
                    inventory.getIngredientByName(recipeIngredient.getIngredientName());
            if (inventoryIngredient == null
                    || recipeIngredient.getAmount() > inventoryIngredient.getAmount()) {
                return false;
            }
        }
        return true;
    }

    private void subtractFromInventory(Recipe recipe) {
        InventoryDto currentInventory = inventoryService.getInventory();
        for (Ingredient recipeIngredient : recipe.getIngredients()) {
            currentInventory.subtractIngredient(
                    recipeIngredient.getIngredientName(),
                    recipeIngredient.getAmount());
        }
        inventoryService.updateInventory(currentInventory);
    }
    
    /**
     * Maps an InventoryDto to an Inventory entity.
     *
     * @param inventoryDto the DTO to map
     * @return the mapped Inventory entity
     */
    private Inventory mapToInventory(InventoryDto inventoryDto) {
        Inventory inventory = new Inventory(inventoryDto.getId());
        for (Ingredient ingredient : inventoryDto.getIngredients()) {
            inventory.addIngredient(ingredient);
        }
        return inventory;
    }
 
    /**
     * Maps a RecipeDto to a Recipe entity.
     *
     * @param recipeDto the DTO to map
     * @return the mapped Recipe entity
     */
    private Recipe mapToRecipe(RecipeDto recipeDto) {
        Recipe recipe = new Recipe(recipeDto.getId(), recipeDto.getName(), recipeDto.getPrice());
        for (Ingredient ingredient : recipeDto.getIngredients()) {
            recipe.addIngredient(ingredient);
        }
        return recipe;
    }

}
