package com.recipe.recipeservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {

    private Long id;

    @NotBlank(message = "Recipe name is required")
    private String name;

    private String description;

    @NotNull(message = "Vegetarian status is required")
    private Boolean vegetarian;

    @NotNull(message = "Number of servings is required")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    private Integer preparationTime;

    private Integer cookingTime;

    @NotEmpty(message = "At least one ingredient is required")
    @Valid
    private List<IngredientDto> ingredients;

    private String createdBy;
}