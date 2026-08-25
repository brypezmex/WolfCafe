package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.IngredientRepository;
import edu.ncsu.csc326.wolfcafe.service.impl.IngredientServiceImpl;

/**
 * Unit tests for IngredientServiceImpl.
 */
@ExtendWith ( MockitoExtension.class )
public class IngredientServiceTest {

    /** Mock ingredient repository. */
    @Mock
    private IngredientRepository  ingredientRepository;

    /** Mock profanity filter; permits any text by default. */
    @Mock
    private ProfanityFilterService profanityFilterService;

    /** Service under test. */
    @InjectMocks
    private IngredientServiceImpl ingredientService;

    /**
     * Injects ModelMapper before each test.
     */
    @BeforeEach
    public void setUp () {
        ReflectionTestUtils.setField( ingredientService, "modelMapper", new ModelMapper() );
    }

    /**
     * Tests creating an ingredient.
     */
    @Test
    public void testCreateIngredient () {
        IngredientDto dto = new IngredientDto();
        dto.setIngredientName( "Coffee" );
        dto.setAmount( 10 );

        Ingredient saved = new Ingredient();
        saved.setId( 1L );
        saved.setIngredientName( "Coffee" );
        saved.setAmount( 10 );

        when( ingredientRepository.save( any( Ingredient.class ) ) ).thenReturn( saved );

        IngredientDto result = ingredientService.createIngredient( dto );

        assertNotNull( result );
        assertEquals( 1L, result.getId() );
        assertEquals( "Coffee", result.getIngredientName() );
        assertEquals( 10, result.getAmount() );
    }

    /**
     * Tests getting ingredient by id.
     */
    @Test
    public void testGetIngredientById () {
        Ingredient ingredient = new Ingredient();
        ingredient.setId( 1L );
        ingredient.setIngredientName( "Milk" );
        ingredient.setAmount( 20 );

        when( ingredientRepository.findById( 1L ) ).thenReturn( Optional.of( ingredient ) );

        IngredientDto result = ingredientService.getIngredientById( 1L );

        assertEquals( 1L, result.getId() );
        assertEquals( "Milk", result.getIngredientName() );
        assertEquals( 20, result.getAmount() );
    }

    /**
     * Tests getting ingredient by missing id.
     */
    @Test
    public void testGetIngredientByIdNotFound () {
        when( ingredientRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> ingredientService.getIngredientById( 99L ) );
    }

    /**
     * Tests getting all ingredients.
     */
    @Test
    public void testGetAllIngredients () {
        Ingredient coffee = new Ingredient();
        coffee.setId( 1L );
        coffee.setIngredientName( "Coffee" );
        coffee.setAmount( 10 );

        Ingredient sugar = new Ingredient();
        sugar.setId( 2L );
        sugar.setIngredientName( "Sugar" );
        sugar.setAmount( 5 );

        when( ingredientRepository.findAll() ).thenReturn( List.of( coffee, sugar ) );

        List<IngredientDto> result = ingredientService.getAllIngredients();

        assertEquals( 2, result.size() );
        assertEquals( "Coffee", result.get( 0 ).getIngredientName() );
        assertEquals( "Sugar", result.get( 1 ).getIngredientName() );
    }

    /**
     * Tests deleting an ingredient.
     */
    @Test
    public void testDeleteIngredient () {
        Ingredient ingredient = new Ingredient();
        ingredient.setId( 1L );
        ingredient.setIngredientName( "Coffee" );
        ingredient.setAmount( 10 );

        when( ingredientRepository.findById( 1L ) ).thenReturn( Optional.of( ingredient ) );

        ingredientService.deleteIngredient( 1L );

        verify( ingredientRepository ).delete( ingredient );
    }

    /**
     * Tests deleting a missing ingredient.
     */
    @Test
    public void testDeleteIngredientNotFound () {
        when( ingredientRepository.findById( 99L ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> ingredientService.deleteIngredient( 99L ) );
    }

    /**
     * Tests deleting all ingredients.
     */
    @Test
    public void testDeleteAllIngredients () {
        ingredientService.deleteAllIngredients();

        verify( ingredientRepository ).deleteAll();
    }
}
