package com.recipe.recipeservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {

    private Long id;

    @NotBlank(message = "Ingredient name is required")
    private String name;

    private String amount;

    private String unit;
}