package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;

public interface IngredientService {

    IngredientDto createIngredient ( IngredientDto ingredientDto );

    IngredientDto getIngredientById ( Long ingredientId );

    List<IngredientDto> getAllIngredients ();

    void deleteIngredient ( Long ingredientId );

    void deleteAllIngredients ();

}
