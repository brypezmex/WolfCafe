package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.service.IngredientService;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;
import edu.ncsu.csc326.wolfcafe.service.MakeRecipeService;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Tests role-based permissions for legacy CoffeeMaker-style endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PermissionControllerTest {

    /** Mock MVC. */
    @Autowired
    private MockMvc                mvc;

    /** Recipe service mock. */
    @MockitoBean
    private RecipeService          recipeService;

    /** Ingredient service mock. */
    @MockitoBean
    private IngredientService      ingredientService;

    /** Inventory service mock. */
    @MockitoBean
    private InventoryService       inventoryService;

    /** Make recipe service mock. */
    @MockitoBean
    private MakeRecipeService      makeRecipeService;

    /** JSON content type. */
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    /**
     * Staff can create a recipe.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testStaffCanCreateRecipe () throws Exception {
        RecipeDto recipe = new RecipeDto();
        recipe.setName( "Mocha" );
        recipe.setPrice( 5.0 );

        Mockito.when( recipeService.isDuplicateName( "Mocha" ) ).thenReturn( false );
        Mockito.when( recipeService.getAllRecipes() ).thenReturn( java.util.List.of() );
        Mockito.when( recipeService.createRecipe( ArgumentMatchers.any() ) ).thenReturn( recipe );

        mvc.perform( post( "/api/recipes" ).contentType( JSON ).content( TestUtils.asJsonString( recipe ) ) )
                .andExpect( status().isOk() );
    }

    /**
     * Customer cannot create a recipe.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCustomerCannotCreateRecipe () throws Exception {
        RecipeDto recipe = new RecipeDto();
        recipe.setName( "Mocha" );
        recipe.setPrice( 5.0 );

        mvc.perform( post( "/api/recipes" ).contentType( JSON ).content( TestUtils.asJsonString( recipe ) ) )
                .andExpect( status().isForbidden() );
    }

    /**
     * Admin should not create recipes if the requirement is staff-only.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testAdminCannotCreateRecipeIfStaffOnly () throws Exception {
        RecipeDto recipe = new RecipeDto();
        recipe.setName( "Mocha" );
        recipe.setPrice( 5.0 );

        mvc.perform( post( "/api/recipes" ).contentType( JSON ).content( TestUtils.asJsonString( recipe ) ) )
                .andExpect( status().isForbidden() );
    }

    /**
     * Staff can delete a recipe.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testStaffCanDeleteRecipe () throws Exception {
        mvc.perform( delete( "/api/recipes/1" ) ).andExpect( status().isOk() );
    }

    /**
     * Customer cannot delete a recipe.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCustomerCannotDeleteRecipe () throws Exception {
        mvc.perform( delete( "/api/recipes/1" ) ).andExpect( status().isForbidden() );
    }

    /**
     * Staff can create an ingredient.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testStaffCanCreateIngredient () throws Exception {
        IngredientDto ingredient = new IngredientDto();
        ingredient.setIngredientName( "Milk" );
        ingredient.setAmount( 10 );

        Mockito.when( ingredientService.createIngredient( ArgumentMatchers.any() ) ).thenReturn( ingredient );

        mvc.perform( post( "/api/ingredients" ).contentType( JSON ).content( TestUtils.asJsonString( ingredient ) ) )
                .andExpect( status().isOk() );
    }

    /**
     * Customer cannot create an ingredient.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCustomerCannotCreateIngredient () throws Exception {
        IngredientDto ingredient = new IngredientDto();
        ingredient.setIngredientName( "Milk" );
        ingredient.setAmount( 10 );

        mvc.perform( post( "/api/ingredients" ).contentType( JSON ).content( TestUtils.asJsonString( ingredient ) ) )
                .andExpect( status().isForbidden() );
    }

    /**
     * Staff can update inventory.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testStaffCanUpdateInventory () throws Exception {
        InventoryDto inventory = new InventoryDto();

        Mockito.when( inventoryService.updateInventory( ArgumentMatchers.any() ) ).thenReturn( inventory );

        mvc.perform( put( "/api/inventory" ).contentType( JSON ).content( TestUtils.asJsonString( inventory ) ) )
                .andExpect( status().isOk() );
    }

    /**
     * Customer cannot update inventory.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCustomerCannotUpdateInventory () throws Exception {
        InventoryDto inventory = new InventoryDto();

        mvc.perform( put( "/api/inventory" ).contentType( JSON ).content( TestUtils.asJsonString( inventory ) ) )
                .andExpect( status().isForbidden() );
    }

    /**
     * Customer can use the old make-recipe purchase endpoint if it remains
     * enabled.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCustomerCanMakeRecipe () throws Exception {
        RecipeDto recipe = new RecipeDto();
        recipe.setName( "Mocha" );
        recipe.setPrice( 5.0 );

        InventoryDto inventory = new InventoryDto();

        Mockito.when( recipeService.getRecipeByName( "Mocha" ) ).thenReturn( recipe );
        Mockito.when( inventoryService.getInventory() ).thenReturn( inventory );
        Mockito.when( makeRecipeService.makeRecipe( ArgumentMatchers.any(), ArgumentMatchers.any() ) )
                .thenReturn( true );

        mvc.perform( post( "/api/makerecipe/Mocha" ).contentType( JSON ).content( "10.0" ) )
                .andExpect( status().isOk() );
    }

    /**
     * Staff cannot use the customer purchase endpoint.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testStaffCannotMakeRecipe () throws Exception {
        mvc.perform( post( "/api/makerecipe/Mocha" ).contentType( JSON ).content( "10.0" ) )
                .andExpect( status().isForbidden() );
    }
}
