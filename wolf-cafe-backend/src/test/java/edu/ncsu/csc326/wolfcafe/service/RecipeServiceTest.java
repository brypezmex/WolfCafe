package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.service.impl.RecipeServiceImpl;

/**
 * Unit tests for RecipeServiceImpl.
 */
@ExtendWith ( MockitoExtension.class )
public class RecipeServiceTest {

    /** Mock recipe repository. */
    @Mock
    private RecipeRepository  recipeRepository;

    /** Mock profanity filter; permits any text by default. */
    @Mock
    private ProfanityFilterService profanityFilterService;

    /** Service under test. */
    @InjectMocks
    private RecipeServiceImpl recipeService;

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
     * Creates a test recipe.
     *
     * @param id
     *            recipe id
     * @param name
     *            recipe name
     * @param price
     *            recipe price
     * @return recipe
     */
    private Recipe recipe ( Long id, String name, double price ) {
        Recipe recipe = new Recipe( id, name, price );
        recipe.addIngredient( ingredient( "Coffee", 2 ) );
        return recipe;
    }

    /**
     * Creates a test recipe dto.
     *
     * @param id
     *            recipe id
     * @param name
     *            recipe name
     * @param price
     *            recipe price
     * @return recipe dto
     */
    private RecipeDto recipeDto ( Long id, String name, double price ) {
        RecipeDto recipe = new RecipeDto( id, name, price );
        recipe.addIngredient( ingredient( "Coffee", 2 ) );
        return recipe;
    }

    /**
     * Tests creating a recipe.
     */
    @Test
    public void testCreateRecipe () {
        RecipeDto request = recipeDto( null, "Mocha", 5.0 );
        Recipe saved = recipe( 1L, "Mocha", 5.0 );

        when( recipeRepository.save( any( Recipe.class ) ) ).thenReturn( saved );

        RecipeDto result = recipeService.createRecipe( request );

        assertEquals( 1L, result.getId() );
        assertEquals( "Mocha", result.getName() );
        assertEquals( 5.0, result.getPrice() );
        assertEquals( 1, result.getIngredients().size() );
    }

    /**
     * Tests getting a recipe by id.
     */
    @Test
    public void testGetRecipeById () {
        when( recipeRepository.findById( 1L ) ).thenReturn( Optional.of( recipe( 1L, "Latte", 4.5 ) ) );

        RecipeDto result = recipeService.getRecipeById( 1L );

        assertEquals( 1L, result.getId() );
        assertEquals( "Latte", result.getName() );
        assertEquals( 4.5, result.getPrice() );
    }

    /**
     * Tests missing recipe by id.
     */
    @Test
    public void testGetRecipeByIdNotFound () {
        when( recipeRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> recipeService.getRecipeById( 99L ) );
    }

    /**
     * Tests getting a recipe by name.
     */
    @Test
    public void testGetRecipeByName () {
        when( recipeRepository.findByName( "Latte" ) ).thenReturn( Optional.of( recipe( 1L, "Latte", 4.5 ) ) );

        RecipeDto result = recipeService.getRecipeByName( "Latte" );

        assertEquals( "Latte", result.getName() );
        assertEquals( 4.5, result.getPrice() );
    }

    /**
     * Tests missing recipe by name.
     */
    @Test
    public void testGetRecipeByNameNotFound () {
        when( recipeRepository.findByName( "Missing" ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> recipeService.getRecipeByName( "Missing" ) );
    }

    /**
     * Tests duplicate name when recipe exists.
     */
    @Test
    public void testIsDuplicateNameTrue () {
        when( recipeRepository.findByName( "Latte" ) ).thenReturn( Optional.of( recipe( 1L, "Latte", 4.5 ) ) );

        assertTrue( recipeService.isDuplicateName( "Latte" ) );
    }

    /**
     * Tests duplicate name when recipe does not exist.
     */
    @Test
    public void testIsDuplicateNameFalse () {
        when( recipeRepository.findByName( "Missing" ) ).thenReturn( Optional.empty() );

        assertFalse( recipeService.isDuplicateName( "Missing" ) );
    }

    /**
     * Tests getting all recipes.
     */
    @Test
    public void testGetAllRecipes () {
        when( recipeRepository.findAll() )
                .thenReturn( List.of( recipe( 1L, "Coffee", 3.0 ), recipe( 2L, "Latte", 4.5 ) ) );

        List<RecipeDto> result = recipeService.getAllRecipes();

        assertEquals( 2, result.size() );
        assertEquals( "Coffee", result.get( 0 ).getName() );
        assertEquals( "Latte", result.get( 1 ).getName() );
    }

    /**
     * Tests updating a recipe.
     */
    @Test
    public void testUpdateRecipe () {
        Recipe existing = recipe( 1L, "Old", 2.0 );
        Recipe saved = recipe( 1L, "New", 6.0 );
        RecipeDto update = recipeDto( 1L, "New", 6.0 );

        when( recipeRepository.findById( 1L ) ).thenReturn( Optional.of( existing ) );
        when( recipeRepository.save( any( Recipe.class ) ) ).thenReturn( saved );

        RecipeDto result = recipeService.updateRecipe( 1L, update );

        assertEquals( 1L, result.getId() );
        assertEquals( "New", result.getName() );
        assertEquals( 6.0, result.getPrice() );
    }

    /**
     * Tests updating a missing recipe.
     */
    @Test
    public void testUpdateRecipeNotFound () {
        RecipeDto update = recipeDto( 99L, "Missing", 6.0 );

        when( recipeRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> recipeService.updateRecipe( 99L, update ) );
    }

    /**
     * Tests deleting a recipe.
     */
    @Test
    public void testDeleteRecipe () {
        Recipe recipe = recipe( 1L, "Coffee", 3.0 );

        when( recipeRepository.findById( 1L ) ).thenReturn( Optional.of( recipe ) );

        recipeService.deleteRecipe( 1L );

        verify( recipeRepository ).delete( recipe );
    }

    /**
     * Tests deleting a missing recipe.
     */
    @Test
    public void testDeleteRecipeNotFound () {
        when( recipeRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> recipeService.deleteRecipe( 99L ) );
    }
}
