package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.service.RecipeService;

/**
 * Controller for Recipes.
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/recipes" )
public class RecipeController {

    /** Connection to RecipeService */
    @Autowired
    private RecipeService recipeService;

    /**
     * REST API method to provide GET access to all recipes in the system
     *
     * @return JSON representation of all recipes
     */
    @GetMapping
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    public List<RecipeDto> getRecipes () {
        return recipeService.getAllRecipes();
    }

    /**
     * REST API method to provide GET access to a specific recipe, as indicated
     * by the path variable provided (the name of the recipe desired)
     *
     * @param name
     *            recipe name
     * @return response to the request
     */
    @GetMapping ( "{name}" )
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')" )
    public ResponseEntity<RecipeDto> getRecipe ( @PathVariable ( "name" ) String name ) {
        RecipeDto recipeDto = recipeService.getRecipeByName( name );
        return ResponseEntity.ok( recipeDto );
    }

    /**
     * REST API method to provide POST access to the Recipe model.
     *
     * @param recipeDto
     *            The valid Recipe to be saved.
     * @return ResponseEntity indicating success if the Recipe could be saved to
     *         the inventory, or an error if it could not be
     */
    @PostMapping
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<RecipeDto> createRecipe ( @RequestBody RecipeDto recipeDto ) {
        if ( recipeService.isDuplicateName( recipeDto.getName() ) ) {
            return new ResponseEntity<>( recipeDto, HttpStatus.CONFLICT );
        }
        if ( recipeService.getAllRecipes().size() < 3 ) {
            RecipeDto savedRecipeDto = recipeService.createRecipe( recipeDto );
            return ResponseEntity.ok( savedRecipeDto );
        }
        else {
            return new ResponseEntity<>( recipeDto, HttpStatus.INSUFFICIENT_STORAGE );
        }
    }

    /**
     * REST API method to allow deleting a Recipe from the CoffeeMaker's
     * Inventory, by making a DELETE request to the API endpoint and indicating
     * the recipe to delete (as a path variable)
     *
     * @param name
     *            The name of the Recipe to delete
     * @return Success if the recipe could be deleted; an error if the recipe
     *         does not exist
     */
    @DeleteMapping ( "{id}" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<String> deleteRecipe ( @PathVariable ( "id" ) Long recipeId ) {
        recipeService.deleteRecipe( recipeId );
        return ResponseEntity.ok( "Recipe deleted successfully." );
    }

    @PutMapping ( "/{id}" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<RecipeDto> updateRecipe ( @PathVariable Long id, @RequestBody RecipeDto recipeDto ) {
        RecipeDto updatedRecipe = recipeService.updateRecipe( id, recipeDto );
        return ResponseEntity.ok( updatedRecipe );
    }

}
