package edu.ncsu.csc326.wolfcafe.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Tests RecipeController branches.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RecipeControllerTest {

    /** Mock MVC. */
    @Autowired
    private MockMvc                   mvc;

    /** Mocked recipe service. */
    @MockitoBean
    private RecipeService             recipeService;

    /** Object mapper. */
    private static final ObjectMapper MAPPER   = new ObjectMapper();

    /** API path. */
    private static final String       API_PATH = "/api/recipes";

    /**
     * Tests getting all recipes.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testGetRecipes () throws Exception {
        RecipeDto coffee = new RecipeDto( 1L, "Coffee", 3.0 );
        RecipeDto latte = new RecipeDto( 2L, "Latte", 4.5 );

        Mockito.when( recipeService.getAllRecipes() ).thenReturn( List.of( coffee, latte ) );

        mvc.perform( get( API_PATH ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$", hasSize( 2 ) ) ).andExpect( jsonPath( "$[0].id", equalTo( 1 ) ) )
                .andExpect( jsonPath( "$[0].name", equalTo( "Coffee" ) ) )
                .andExpect( jsonPath( "$[0].price", equalTo( 3.0 ) ) ).andExpect( jsonPath( "$[1].id", equalTo( 2 ) ) )
                .andExpect( jsonPath( "$[1].name", equalTo( "Latte" ) ) )
                .andExpect( jsonPath( "$[1].price", equalTo( 4.5 ) ) );
    }

    /**
     * Tests getting one recipe by name.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testGetRecipeByName () throws Exception {
        RecipeDto recipe = new RecipeDto( 1L, "Coffee", 3.0 );

        Mockito.when( recipeService.getRecipeByName( "Coffee" ) ).thenReturn( recipe );

        mvc.perform( get( API_PATH + "/Coffee" ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", equalTo( 1 ) ) ).andExpect( jsonPath( "$.name", equalTo( "Coffee" ) ) )
                .andExpect( jsonPath( "$.price", equalTo( 3.0 ) ) );
    }

    /**
     * Tests successful recipe creation.
     *
     * Covers: duplicate name false recipe count less than 3 true
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testCreateRecipeSuccess () throws Exception {
        RecipeDto request = new RecipeDto( null, "Mocha", 5.0 );
        RecipeDto response = new RecipeDto( 1L, "Mocha", 5.0 );

        Mockito.when( recipeService.isDuplicateName( "Mocha" ) ).thenReturn( false );
        Mockito.when( recipeService.getAllRecipes() ).thenReturn( List.of() );
        Mockito.when( recipeService.createRecipe( any( RecipeDto.class ) ) ).thenReturn( response );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON )
                .content( MAPPER.writeValueAsString( request ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.id", equalTo( 1 ) ) )
                .andExpect( jsonPath( "$.name", equalTo( "Mocha" ) ) )
                .andExpect( jsonPath( "$.price", equalTo( 5.0 ) ) );
    }

    /**
     * Tests duplicate recipe creation.
     *
     * Covers: duplicate name true
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testCreateRecipeDuplicateName () throws Exception {
        RecipeDto request = new RecipeDto( null, "Coffee", 3.0 );

        Mockito.when( recipeService.isDuplicateName( "Coffee" ) ).thenReturn( true );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON )
                .content( MAPPER.writeValueAsString( request ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isConflict() ).andExpect( jsonPath( "$.name", equalTo( "Coffee" ) ) )
                .andExpect( jsonPath( "$.price", equalTo( 3.0 ) ) );

        Mockito.verify( recipeService, Mockito.never() ).createRecipe( any( RecipeDto.class ) );
    }

    /**
     * Tests recipe creation when max recipe count has already been reached.
     *
     * Covers: duplicate name false recipe count less than 3 false
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testCreateRecipeMaxRecipesReached () throws Exception {
        RecipeDto request = new RecipeDto( null, "Fourth", 6.0 );

        RecipeDto recipe1 = new RecipeDto( 1L, "Coffee", 3.0 );
        RecipeDto recipe2 = new RecipeDto( 2L, "Latte", 4.0 );
        RecipeDto recipe3 = new RecipeDto( 3L, "Mocha", 5.0 );

        Mockito.when( recipeService.isDuplicateName( "Fourth" ) ).thenReturn( false );
        Mockito.when( recipeService.getAllRecipes() ).thenReturn( List.of( recipe1, recipe2, recipe3 ) );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON )
                .content( MAPPER.writeValueAsString( request ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isInsufficientStorage() ).andExpect( jsonPath( "$.name", equalTo( "Fourth" ) ) )
                .andExpect( jsonPath( "$.price", equalTo( 6.0 ) ) );

        Mockito.verify( recipeService, Mockito.never() ).createRecipe( any( RecipeDto.class ) );
    }

    /**
     * Tests deleting a recipe.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testDeleteRecipe () throws Exception {
        Mockito.doNothing().when( recipeService ).deleteRecipe( eq( 1L ) );

        mvc.perform( delete( API_PATH + "/1" ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() );

        Mockito.verify( recipeService ).deleteRecipe( eq( 1L ) );
    }

    /**
     * Tests updating a recipe.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testUpdateRecipe () throws Exception {
        RecipeDto request = new RecipeDto( 1L, "Updated", 7.0 );
        RecipeDto response = new RecipeDto( 1L, "Updated", 7.0 );

        Mockito.when( recipeService.updateRecipe( eq( 1L ), any( RecipeDto.class ) ) ).thenReturn( response );

        mvc.perform( put( API_PATH + "/1" ).contentType( MediaType.APPLICATION_JSON )
                .content( MAPPER.writeValueAsString( request ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.id", equalTo( 1 ) ) )
                .andExpect( jsonPath( "$.name", equalTo( "Updated" ) ) )
                .andExpect( jsonPath( "$.price", equalTo( 7.0 ) ) );
    }
}
