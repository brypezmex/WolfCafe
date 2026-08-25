package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.service.impl.MakeRecipeServiceImpl;

/**
 * Unit tests for MakeRecipeServiceImpl.
 */
@ExtendWith ( MockitoExtension.class )
public class MakeRecipeServiceTest {

    /** Mock inventory service. */
    @Mock
    private InventoryService      inventoryService;

    /** Service under test. */
    @InjectMocks
    private MakeRecipeServiceImpl makeRecipeService;

    /**
     * Creates a test ingredient.
     *
     * @param name
     *            ingredient name
     * @param amount
     *            amount
     * @return ingredient
     */
    private Ingredient ingredient ( String name, int amount ) {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientName( name );
        ingredient.setAmount( amount );
        return ingredient;
    }

    /**
     * Creates inventory dto.
     *
     * @param coffee
     *            coffee amount
     * @param milk
     *            milk amount
     * @return inventory dto
     */
    private InventoryDto inventory ( int coffee, int milk ) {
        InventoryDto inventory = new InventoryDto();
        inventory.setId( 1L );
        inventory.addIngredient( ingredient( "Coffee", coffee ) );
        inventory.addIngredient( ingredient( "Milk", milk ) );
        return inventory;
    }

    /**
     * Creates recipe dto.
     *
     * @param coffee
     *            coffee amount
     * @param milk
     *            milk amount
     * @return recipe dto
     */
    private RecipeDto recipe ( int coffee, int milk ) {
        RecipeDto recipe = new RecipeDto( 1L, "Latte", 5.0 );
        recipe.addIngredient( ingredient( "Coffee", coffee ) );
        recipe.addIngredient( ingredient( "Milk", milk ) );
        return recipe;
    }

    /**
     * Tests making a recipe successfully.
     */
    @Test
    public void testMakeRecipeEnoughIngredients () {
        InventoryDto inventory = inventory( 10, 10 );
        RecipeDto recipe = recipe( 2, 3 );

        InventoryDto currentInventory = inventory( 10, 10 );
        when( inventoryService.getInventory() ).thenReturn( currentInventory );
        when( inventoryService.updateInventory( any( InventoryDto.class ) ) ).thenReturn( currentInventory );

        boolean result = makeRecipeService.makeRecipe( inventory, recipe );

        assertTrue( result );
        verify( inventoryService ).getInventory();
        verify( inventoryService ).updateInventory( any( InventoryDto.class ) );
    }

    /**
     * Tests making a recipe when an ingredient amount is too low.
     */
    @Test
    public void testMakeRecipeNotEnoughIngredientAmount () {
        InventoryDto inventory = inventory( 1, 10 );
        RecipeDto recipe = recipe( 2, 3 );

        boolean result = makeRecipeService.makeRecipe( inventory, recipe );

        assertFalse( result );
        verify( inventoryService, never() ).getInventory();
        verify( inventoryService, never() ).updateInventory( any( InventoryDto.class ) );
    }

    /**
     * Tests making a recipe when an ingredient is missing.
     */
    @Test
    public void testMakeRecipeMissingIngredient () {
        InventoryDto inventory = new InventoryDto();
        inventory.setId( 1L );
        inventory.addIngredient( ingredient( "Coffee", 10 ) );

        RecipeDto recipe = new RecipeDto( 1L, "Latte", 5.0 );
        recipe.addIngredient( ingredient( "Coffee", 2 ) );
        recipe.addIngredient( ingredient( "Milk", 3 ) );

        boolean result = makeRecipeService.makeRecipe( inventory, recipe );

        assertFalse( result );
        verify( inventoryService, never() ).getInventory();
        verify( inventoryService, never() ).updateInventory( any( InventoryDto.class ) );
    }

    /**
     * Tests making a recipe with no required ingredients.
     */
    @Test
    public void testMakeRecipeWithNoRecipeIngredients () {
        InventoryDto inventory = inventory( 10, 10 );
        RecipeDto recipe = new RecipeDto( 1L, "Water", 1.0 );

        InventoryDto currentInventory = inventory( 10, 10 );
        when( inventoryService.getInventory() ).thenReturn( currentInventory );
        when( inventoryService.updateInventory( any( InventoryDto.class ) ) ).thenReturn( currentInventory );

        boolean result = makeRecipeService.makeRecipe( inventory, recipe );

        assertTrue( result );
        verify( inventoryService ).getInventory();
        verify( inventoryService ).updateInventory( any( InventoryDto.class ) );
    }
}
