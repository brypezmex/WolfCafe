package edu.ncsu.csc326.wolfcafe.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import edu.ncsu.csc326.wolfcafe.service.MakeRecipeService;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Tests MakeRecipeController branches.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MakeRecipeControllerTest {

    /** Mock MVC. */
    @Autowired
    private MockMvc             mvc;

    /** Mocked inventory service. */
    @MockitoBean
    private InventoryService    inventoryService;

    /** Mocked recipe service. */
    @MockitoBean
    private RecipeService       recipeService;

    /** Mocked make recipe service. */
    @MockitoBean
    private MakeRecipeService   makeRecipeService;

    /** API path. */
    private static final String API_PATH = "/api/makerecipe";

    /**
     * Creates a simple inventory.
     *
     * @return inventory dto
     */
    private InventoryDto inventory () {
        InventoryDto inventory = new InventoryDto( 1L );
        inventory.addIngredient( new Ingredient( "Coffee", 10 ) );
        inventory.addIngredient( new Ingredient( "Milk", 10 ) );
        return inventory;
    }

    /**
     * Tests successful recipe purchase.
     *
     * Covers: price <= amount paid true makeRecipeService.makeRecipe true
     * change != amount paid
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testMakeRecipeSuccess () throws Exception {
        RecipeDto recipe = new RecipeDto( 1L, "Coffee", 3.0 );

        Mockito.when( recipeService.getRecipeByName( eq( "Coffee" ) ) ).thenReturn( recipe );
        Mockito.when( inventoryService.getInventory() ).thenReturn( inventory() );
        Mockito.when( makeRecipeService.makeRecipe( any( InventoryDto.class ), eq( recipe ) ) ).thenReturn( true );

        mvc.perform( post( API_PATH + "/Coffee" ).contentType( MediaType.APPLICATION_JSON ).content( "5.0" )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( content().string( equalTo( "2.0" ) ) );
    }

    /**
     * Tests payment lower than recipe price.
     *
     * Covers: price <= amount paid false change == amount paid amount paid <
     * price true
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testMakeRecipeInsufficientPayment () throws Exception {
        RecipeDto recipe = new RecipeDto( 1L, "Coffee", 3.0 );

        Mockito.when( recipeService.getRecipeByName( eq( "Coffee" ) ) ).thenReturn( recipe );
        Mockito.when( inventoryService.getInventory() ).thenReturn( inventory() );

        mvc.perform( post( API_PATH + "/Coffee" ).contentType( MediaType.APPLICATION_JSON ).content( "2.0" )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isConflict() )
                .andExpect( content().string( equalTo( "2.0" ) ) );

        Mockito.verify( makeRecipeService, Mockito.never() ).makeRecipe( any( InventoryDto.class ),
                any( RecipeDto.class ) );
    }

    /**
     * Tests enough payment but not enough inventory.
     *
     * Covers: price <= amount paid true makeRecipeService.makeRecipe false
     * change == amount paid amount paid < price false
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testMakeRecipeInsufficientInventory () throws Exception {
        RecipeDto recipe = new RecipeDto( 1L, "Coffee", 3.0 );

        Mockito.when( recipeService.getRecipeByName( eq( "Coffee" ) ) ).thenReturn( recipe );
        Mockito.when( inventoryService.getInventory() ).thenReturn( inventory() );
        Mockito.when( makeRecipeService.makeRecipe( any( InventoryDto.class ), eq( recipe ) ) ).thenReturn( false );

        mvc.perform( post( API_PATH + "/Coffee" ).contentType( MediaType.APPLICATION_JSON ).content( "5.0" )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isBadRequest() )
                .andExpect( content().string( equalTo( "5.0" ) ) );
    }

    /**
     * Tests exact payment.
     *
     * This adds coverage for the edge case where the recipe is made
     * successfully and the change is zero.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testMakeRecipeExactPayment () throws Exception {
        RecipeDto recipe = new RecipeDto( 1L, "Coffee", 3.0 );

        Mockito.when( recipeService.getRecipeByName( eq( "Coffee" ) ) ).thenReturn( recipe );
        Mockito.when( inventoryService.getInventory() ).thenReturn( inventory() );
        Mockito.when( makeRecipeService.makeRecipe( any( InventoryDto.class ), eq( recipe ) ) ).thenReturn( true );

        mvc.perform( post( API_PATH + "/Coffee" ).contentType( MediaType.APPLICATION_JSON ).content( "3.0" )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( content().string( equalTo( "0.0" ) ) );
    }
}
