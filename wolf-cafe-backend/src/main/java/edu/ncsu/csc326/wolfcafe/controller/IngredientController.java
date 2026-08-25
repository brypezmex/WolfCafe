package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.service.IngredientService;

@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/ingredients" )
public class IngredientController {

    @Autowired
    private IngredientService ingredientService;

    /**
     * Gets all ingredients.
     *
     * @return list of ingredients
     */
    @GetMapping
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF')" )
    public ResponseEntity<List<IngredientDto>> getAllIngredients () {
        List<IngredientDto> ingredients = ingredientService.getAllIngredients();
        return ResponseEntity.ok( ingredients );
    }

    /**
     * Gets an ingredient by id.
     *
     * @param id
     *            ingredient id
     * @return ingredient
     */
    @GetMapping ( "{id}" )
    @PreAuthorize ( "hasAnyRole('ADMIN', 'STAFF')" )
    public ResponseEntity<IngredientDto> getIngredient ( @PathVariable ( "id" ) Long id ) {
        IngredientDto ingredientDto = ingredientService.getIngredientById( id );
        return ResponseEntity.ok( ingredientDto );
    }

    /**
     * Creates an ingredient.
     *
     * @param ingredientDto
     *            ingredient to create
     * @return created ingredient
     */
    @PostMapping
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<IngredientDto> createIngredient ( @RequestBody IngredientDto ingredientDto ) {
        IngredientDto savedIngredientDto = ingredientService.createIngredient( ingredientDto );
        return ResponseEntity.ok( savedIngredientDto );
    }

    /**
     * Deletes an ingredient by id.
     *
     * @param id
     *            ingredient id
     * @return success message
     */
    @DeleteMapping ( "{id}" )
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<String> deleteIngredient ( @PathVariable ( "id" ) Long id ) {
        ingredientService.deleteIngredient( id );
        return ResponseEntity.ok( "Ingredient deleted successfully." );
    }

}
