package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.service.ProfanityFilterService;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Implementation of the RecipeService interface.
 */
@Service
public class RecipeServiceImpl implements RecipeService {

    /** Connection to the repository to work with the DAO + database */
    @Autowired
    private RecipeRepository recipeRepository;

    /** Screens user-supplied text for profanity */
    @Autowired
    private ProfanityFilterService profanityFilterService;

    /**
     * Creates a recipe with the given information.
     *
     * @param recipeDto
     *            recipe to create
     * @return created recipe
     */
    @Override
    public RecipeDto createRecipe ( RecipeDto recipeDto ) {
        validateLanguage( recipeDto );
        Recipe recipe = mapToRecipe( recipeDto );
        Recipe savedRecipe = recipeRepository.save( recipe );
        return mapToRecipeDto( savedRecipe );
    }

    /**
     * Returns the recipe with the given id.
     *
     * @param recipeId
     *            recipe id
     * @return recipe
     */
    @Override
    public RecipeDto getRecipeById ( Long recipeId ) {
        Recipe recipe = recipeRepository.findById( recipeId )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with id " + recipeId ) );
        return mapToRecipeDto( recipe );
    }

    /**
     * Returns the recipe with the given name.
     *
     * @param recipeName
     *            recipe name
     * @return recipe
     */
    @Override
    public RecipeDto getRecipeByName ( String recipeName ) {
        Recipe recipe = recipeRepository.findByName( recipeName )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with name " + recipeName ) );
        return mapToRecipeDto( recipe );
    }

    /**
     * Returns true if the recipe already exists in the database.
     *
     * @param recipeName
     *            recipe name to check
     * @return true if duplicate
     */
    @Override
    public boolean isDuplicateName ( String recipeName ) {
        try {
            getRecipeByName( recipeName );
            return true;
        }
        catch ( ResourceNotFoundException e ) {
            return false;
        }
    }

    /**
     * Returns all recipes.
     *
     * @return all recipes
     */
    @Override
    public List<RecipeDto> getAllRecipes () {
        List<Recipe> recipes = recipeRepository.findAll();
        return recipes.stream().map( this::mapToRecipeDto ).collect( Collectors.toList() );
    }

    /**
     * Updates recipe name, price, and ingredients.
     *
     * @param recipeId
     *            recipe id
     * @param recipeDto
     *            updated recipe values
     * @return updated recipe
     */
    @Override
    public RecipeDto updateRecipe ( Long recipeId, RecipeDto recipeDto ) {
        validateLanguage( recipeDto );
        Recipe recipe = recipeRepository.findById( recipeId )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with id " + recipeId ) );

        recipe.setName( recipeDto.getName() );
        recipe.setPrice( recipeDto.getPrice() );

        /*
         * Important: staff can edit recipe ingredients in the frontend, so the
         * backend must also persist ingredient changes.
         */
        recipe.getIngredients().clear();
        if ( recipeDto.getIngredients() != null ) {
            for ( Ingredient ingredient : recipeDto.getIngredients() ) {
                recipe.addIngredient( ingredient );
            }
        }

        Recipe savedRecipe = recipeRepository.save( recipe );
        return mapToRecipeDto( savedRecipe );
    }

    /**
     * Screens the recipe's name and its ingredient names for profanity.
     *
     * @param recipeDto
     *            recipe whose text should be screened
     */
    private void validateLanguage ( RecipeDto recipeDto ) {
        profanityFilterService.validate( "Drink name", recipeDto.getName() );

        for ( Ingredient ingredient : recipeDto.getIngredients() ) {
            profanityFilterService.validate( "Ingredient name", ingredient.getIngredientName() );
        }
    }

    /**
     * Deletes the recipe with the given id.
     *
     * @param recipeId
     *            recipe id
     */
    @Override
    public void deleteRecipe ( Long recipeId ) {
        Recipe recipe = recipeRepository.findById( recipeId )
                .orElseThrow( () -> new ResourceNotFoundException( "Recipe does not exist with id " + recipeId ) );

        recipeRepository.delete( recipe );
    }

    /**
     * Maps a Recipe entity to a RecipeDto.
     *
     * @param recipe
     *            entity to map
     * @return mapped RecipeDto
     */
    private RecipeDto mapToRecipeDto ( Recipe recipe ) {
        RecipeDto dto = new RecipeDto( recipe.getId(), recipe.getName(), recipe.getPrice() );

        if ( recipe.getIngredients() != null ) {
            for ( Ingredient ingredient : recipe.getIngredients() ) {
                dto.addIngredient( ingredient );
            }
        }

        return dto;
    }

    /**
     * Maps a RecipeDto to a Recipe entity.
     *
     * @param recipeDto
     *            DTO to map
     * @return mapped Recipe entity
     */
    private Recipe mapToRecipe ( RecipeDto recipeDto ) {
        Recipe recipe = new Recipe( recipeDto.getId(), recipeDto.getName(), recipeDto.getPrice() );

        if ( recipeDto.getIngredients() != null ) {
            for ( Ingredient ingredient : recipeDto.getIngredients() ) {
                recipe.addIngredient( ingredient );
            }
        }

        return recipe;
    }
}
