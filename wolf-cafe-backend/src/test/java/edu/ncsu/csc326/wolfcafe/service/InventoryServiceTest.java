package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.service.impl.InventoryServiceImpl;

/**
 * Unit tests for InventoryServiceImpl.
 */
@ExtendWith ( MockitoExtension.class )
public class InventoryServiceTest {

    /** Mock inventory repository. */
    @Mock
    private InventoryRepository  inventoryRepository;

    /** Mock profanity filter; permits any text by default. */
    @Mock
    private ProfanityFilterService profanityFilterService;

    /** Service under test. */
    @InjectMocks
    private InventoryServiceImpl inventoryService;

    /**
     * Creates a test ingredient.
     *
     * @param name
     *            ingredient name
     * @param amount
     *            ingredient amount
     * @return ingredient
     */
    private Ingredient ingredient ( String name, int amount ) {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientName( name );
        ingredient.setAmount( amount );
        return ingredient;
    }

    /**
     * Tests creating inventory.
     */
    @Test
    public void testCreateInventory () {
        InventoryDto dto = new InventoryDto();
        dto.setId( 1L );
        dto.addIngredient( ingredient( "Coffee", 10 ) );
        dto.addIngredient( ingredient( "Milk", 20 ) );

        Inventory saved = new Inventory( 1L );
        saved.addIngredient( ingredient( "Coffee", 10 ) );
        saved.addIngredient( ingredient( "Milk", 20 ) );

        when( inventoryRepository.save( any( Inventory.class ) ) ).thenReturn( saved );

        InventoryDto result = inventoryService.createInventory( dto );

        assertEquals( 1L, result.getId() );
        assertEquals( 2, result.getIngredients().size() );
        assertEquals( "Coffee", result.getIngredients().get( 0 ).getIngredientName() );
    }

    /**
     * Tests getting existing inventory.
     */
    @Test
    public void testGetExistingInventory () {
        Inventory inventory = new Inventory( 1L );
        inventory.addIngredient( ingredient( "Coffee", 10 ) );

        when( inventoryRepository.findAll() ).thenReturn( List.of( inventory ) );

        InventoryDto result = inventoryService.getInventory();

        assertNotNull( result );
        assertEquals( 1L, result.getId() );
        assertEquals( 1, result.getIngredients().size() );
        assertEquals( "Coffee", result.getIngredients().get( 0 ).getIngredientName() );
    }

    /**
     * Tests getting inventory when none exists creates a new inventory.
     */
    @Test
    public void testGetInventoryCreatesInventoryWhenEmpty () {
        Inventory saved = new Inventory( 1L );

        when( inventoryRepository.findAll() ).thenReturn( List.of() );
        when( inventoryRepository.save( any( Inventory.class ) ) ).thenReturn( saved );

        InventoryDto result = inventoryService.getInventory();

        assertNotNull( result );
        assertEquals( 1L, result.getId() );
        assertEquals( 0, result.getIngredients().size() );
    }

    /**
     * Tests updating inventory.
     */
    @Test
    public void testUpdateInventory () {
        Inventory existing = new Inventory( 1L );
        existing.addIngredient( ingredient( "Coffee", 1 ) );

        InventoryDto update = new InventoryDto();
        update.setId( 1L );
        update.addIngredient( ingredient( "Coffee", 30 ) );
        update.addIngredient( ingredient( "Milk", 15 ) );

        Inventory saved = new Inventory( 1L );
        saved.addIngredient( ingredient( "Coffee", 30 ) );
        saved.addIngredient( ingredient( "Milk", 15 ) );

        when( inventoryRepository.findAll() ).thenReturn( List.of( existing ) );
        when( inventoryRepository.save( any( Inventory.class ) ) ).thenReturn( saved );

        InventoryDto result = inventoryService.updateInventory( update );

        assertEquals( 1L, result.getId() );
        assertEquals( 2, result.getIngredients().size() );
        assertEquals( 30, result.getIngredientByName( "Coffee" ).getAmount() );
        assertEquals( 15, result.getIngredientByName( "Milk" ).getAmount() );
    }

    /**
     * Updating when the inventory table is empty creates the single inventory
     * row instead of failing. The row's id is database-generated, so the update
     * must never assume a particular id.
     */
    @Test
    public void testUpdateInventoryCreatesInventoryWhenMissing () {
        InventoryDto update = new InventoryDto();
        update.addIngredient( ingredient( "Coffee", 5 ) );

        Inventory created = new Inventory( 7L );

        Inventory saved = new Inventory( 7L );
        saved.addIngredient( ingredient( "Coffee", 5 ) );

        when( inventoryRepository.findAll() ).thenReturn( List.of() );
        when( inventoryRepository.save( any( Inventory.class ) ) ).thenReturn( created, saved );

        InventoryDto result = inventoryService.updateInventory( update );

        assertEquals( 7L, result.getId() );
        assertEquals( 1, result.getIngredients().size() );
        assertEquals( 5, result.getIngredientByName( "Coffee" ).getAmount() );
    }
}
