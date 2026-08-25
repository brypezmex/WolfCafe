package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.IngredientRepository;
import edu.ncsu.csc326.wolfcafe.service.IngredientService;
import edu.ncsu.csc326.wolfcafe.service.ProfanityFilterService;

@Service
public class IngredientServiceImpl implements IngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;
    
    /** ModelMapper for converting between entity and DTO */
    @Autowired
    private ModelMapper modelMapper;

    /** Screens user-supplied text for profanity */
    @Autowired
    private ProfanityFilterService profanityFilterService;

    public IngredientDto createIngredient ( IngredientDto ingredientDto ) {
        profanityFilterService.validate( "Ingredient name", ingredientDto.getIngredientName() );
        Ingredient ingredient = modelMapper.map(ingredientDto, Ingredient.class);
        Ingredient savedIngredient = ingredientRepository.save( ingredient );
        return modelMapper.map(savedIngredient, IngredientDto.class);
    }

    public IngredientDto getIngredientById ( Long ingredientId ) {
        Ingredient ingredient = ingredientRepository.findById( ingredientId ).orElseThrow(
                () -> new ResourceNotFoundException( "Ingredient does not exist with id " + ingredientId ) );
        return modelMapper.map(ingredient, IngredientDto.class);
    }

    public List<IngredientDto> getAllIngredients () {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        return ingredients.stream()
                .map(ingredient -> modelMapper.map(ingredient, IngredientDto.class))
                .collect(Collectors.toList());
    }

    public void deleteIngredient ( Long ingredientId ) {
        Ingredient ingredient = ingredientRepository.findById( ingredientId ).orElseThrow(
                () -> new ResourceNotFoundException( "Ingredient does not exist with id " + ingredientId ) );

        ingredientRepository.delete( ingredient );
    }

    public void deleteAllIngredients () {
        ingredientRepository.deleteAll();
    }

}
