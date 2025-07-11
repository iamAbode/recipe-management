package com.recipe.recipeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeFilterDto {

    private Boolean vegetarian;
    private Integer servings;
    private String includeIngredient;
    private String excludeIngredient;
    private String instructionText;
}