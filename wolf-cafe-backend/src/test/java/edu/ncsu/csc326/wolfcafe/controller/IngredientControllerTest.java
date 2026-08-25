package edu.ncsu.csc326.wolfcafe.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.service.IngredientService;

/**
 * Tests IngredientController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class IngredientControllerTest {

    /** Mock MVC. */
    @Autowired
    private MockMvc                   mvc;

    /** Mocked ingredient service. */
    @MockitoBean
    private IngredientService         ingredientService;

    /** Object mapper. */
    private static final ObjectMapper MAPPER   = new ObjectMapper();

    /** API path. */
    private static final String       API_PATH = "/api/ingredients";

    /**
     * Tests getting all ingredients as staff.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testGetAllIngredients () throws Exception {
        IngredientDto coffee = new IngredientDto();
        coffee.setId( 1L );
        coffee.setIngredientName( "Coffee" );
        coffee.setAmount( 10 );

        IngredientDto milk = new IngredientDto();
        milk.setId( 2L );
        milk.setIngredientName( "Milk" );
        milk.setAmount( 20 );

        Mockito.when( ingredientService.getAllIngredients() ).thenReturn( List.of( coffee, milk ) );

        mvc.perform( get( API_PATH ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$", hasSize( 2 ) ) ).andExpect( jsonPath( "$[0].id", equalTo( 1 ) ) )
                .andExpect( jsonPath( "$[0].ingredientName", equalTo( "Coffee" ) ) )
                .andExpect( jsonPath( "$[0].amount", equalTo( 10 ) ) ).andExpect( jsonPath( "$[1].id", equalTo( 2 ) ) )
                .andExpect( jsonPath( "$[1].ingredientName", equalTo( "Milk" ) ) )
                .andExpect( jsonPath( "$[1].amount", equalTo( 20 ) ) );
    }

    /**
     * Tests getting an ingredient by id as staff.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testGetIngredientById () throws Exception {
        IngredientDto ingredient = new IngredientDto();
        ingredient.setId( 1L );
        ingredient.setIngredientName( "Coffee" );
        ingredient.setAmount( 10 );

        Mockito.when( ingredientService.getIngredientById( eq( 1L ) ) ).thenReturn( ingredient );

        mvc.perform( get( API_PATH + "/1" ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id", equalTo( 1 ) ) )
                .andExpect( jsonPath( "$.ingredientName", equalTo( "Coffee" ) ) )
                .andExpect( jsonPath( "$.amount", equalTo( 10 ) ) );
    }

    /**
     * Tests creating an ingredient as staff.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testCreateIngredient () throws Exception {
        IngredientDto request = new IngredientDto();
        request.setIngredientName( "Sugar" );
        request.setAmount( 15 );

        IngredientDto response = new IngredientDto();
        response.setId( 3L );
        response.setIngredientName( "Sugar" );
        response.setAmount( 15 );

        Mockito.when( ingredientService.createIngredient( any( IngredientDto.class ) ) ).thenReturn( response );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON )
                .content( MAPPER.writeValueAsString( request ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.id", equalTo( 3 ) ) )
                .andExpect( jsonPath( "$.ingredientName", equalTo( "Sugar" ) ) )
                .andExpect( jsonPath( "$.amount", equalTo( 15 ) ) );
    }

    /**
     * Tests deleting an ingredient as staff.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "staff1", roles = "STAFF" )
    public void testDeleteIngredient () throws Exception {
        Mockito.doNothing().when( ingredientService ).deleteIngredient( eq( 1L ) );

        mvc.perform( delete( API_PATH + "/1" ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() );

        Mockito.verify( ingredientService ).deleteIngredient( eq( 1L ) );
    }

    /**
     * Tests that customers cannot create ingredients.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCustomerCannotCreateIngredient () throws Exception {
        IngredientDto request = new IngredientDto();
        request.setIngredientName( "Chocolate" );
        request.setAmount( 5 );

        mvc.perform( post( API_PATH ).contentType( MediaType.APPLICATION_JSON )
                .content( MAPPER.writeValueAsString( request ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }

    /**
     * Tests that customers cannot delete ingredients.
     *
     * @throws Exception
     *             if request fails
     */
    @Test
    @WithMockUser ( username = "customer1", roles = "CUSTOMER" )
    public void testCustomerCannotDeleteIngredient () throws Exception {
        mvc.perform( delete( API_PATH + "/1" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isForbidden() );
    }
}
