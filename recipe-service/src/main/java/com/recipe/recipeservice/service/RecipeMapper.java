package com.recipe.recipeservice.service;

import com.recipe.recipeservice.dto.IngredientDto;
import com.recipe.recipeservice.dto.RecipeDto;
import com.recipe.recipeservice.entity.Ingredient;
import com.recipe.recipeservice.entity.Recipe;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    public RecipeDto toDto(Recipe recipe) {
        return RecipeDto.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .vegetarian(recipe.isVegetarian())
                .servings(recipe.getServings())
                .instructions(recipe.getInstructions())
                .preparationTime(recipe.getPreparationTime())
                .cookingTime(recipe.getCookingTime())
                .ingredients(recipe.getIngredients().stream()
                        .map(this::toIngredientDto)
                        .collect(Collectors.toList()))
                .createdBy(recipe.getCreatedBy())
                .build();
    }

    public Recipe toEntity(RecipeDto recipeDto) {
        Recipe recipe = Recipe.builder()
                .id(recipeDto.getId())
                .name(recipeDto.getName())
                .description(recipeDto.getDescription())
                .vegetarian(recipeDto.getVegetarian())
                .servings(recipeDto.getServings())
                .instructions(recipeDto.getInstructions())
                .preparationTime(recipeDto.getPreparationTime())
                .cookingTime(recipeDto.getCookingTime())
                .createdBy(recipeDto.getCreatedBy())
                .build();

        recipeDto.getIngredients().forEach(ingredientDto -> {
            Ingredient ingredient = toIngredientEntity(ingredientDto);
            recipe.addIngredient(ingredient);
        });

        return recipe;
    }

    public IngredientDto toIngredientDto(Ingredient ingredient) {
        return IngredientDto.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .amount(ingredient.getAmount())
                .unit(ingredient.getUnit())
                .build();
    }

    public Ingredient toIngredientEntity(IngredientDto ingredientDto) {
        return Ingredient.builder()
                .id(ingredientDto.getId())
                .name(ingredientDto.getName())
                .amount(ingredientDto.getAmount())
                .unit(ingredientDto.getUnit())
                .build();
    }
}