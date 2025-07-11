package com.recipe.recipeservice.repository;

import com.recipe.recipeservice.entity.Ingredient;
import com.recipe.recipeservice.entity.Recipe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    void findByVegetarian_ShouldReturnVegetarianRecipes() {
        // Arrange
        Recipe vegetarianRecipe = createTestRecipe("Vegetarian Recipe", true, 4);
        Recipe nonVegetarianRecipe = createTestRecipe("Non-Vegetarian Recipe", false, 2);
        
        recipeRepository.saveAll(List.of(vegetarianRecipe, nonVegetarianRecipe));
        
        // Act
        List<Recipe> result = recipeRepository.findByVegetarian(true);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("Vegetarian Recipe", result.get(0).getName());
        assertTrue(result.get(0).isVegetarian());
    }
    
    @Test
    void findByServings_ShouldReturnRecipesWithSpecifiedServings() {
        // Arrange
        Recipe recipe1 = createTestRecipe("Recipe for 2", true, 2);
        Recipe recipe2 = createTestRecipe("Recipe for 4", false, 4);
        
        recipeRepository.saveAll(List.of(recipe1, recipe2));
        
        // Act
        List<Recipe> result = recipeRepository.findByServings(4);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("Recipe for 4", result.get(0).getName());
        assertEquals(4, result.get(0).getServings());
    }
    
    @Test
    void findByIngredientNameContaining_ShouldReturnRecipesWithSpecifiedIngredient() {
        // Arrange
        Recipe recipe1 = createTestRecipe("Recipe with Potato", true, 2);
        addIngredientToRecipe(recipe1, "Potato", "500", "g");
        
        Recipe recipe2 = createTestRecipe("Recipe with Carrot", true, 4);
        addIngredientToRecipe(recipe2, "Carrot", "300", "g");
        
        recipeRepository.saveAll(List.of(recipe1, recipe2));
        
        // Act
        List<Recipe> result = recipeRepository.findByIngredientNameContaining("Potato");
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("Recipe with Potato", result.get(0).getName());
    }
    
    @Test
    void findByInstructionsContaining_ShouldReturnRecipesWithSpecifiedInstructionText() {
        // Arrange
        Recipe recipe1 = createTestRecipe("Recipe 1", true, 2);
        recipe1.setInstructions("Bake in oven for 30 minutes");
        
        Recipe recipe2 = createTestRecipe("Recipe 2", true, 4);
        recipe2.setInstructions("Fry in a pan until golden");
        
        recipeRepository.saveAll(List.of(recipe1, recipe2));
        
        // Act
        List<Recipe> result = recipeRepository.findByInstructionsContaining("oven");
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("Recipe 1", result.get(0).getName());
        assertTrue(result.get(0).getInstructions().contains("oven"));
    }
    
    private Recipe createTestRecipe(String name, boolean vegetarian, int servings) {
        Recipe recipe = Recipe.builder()
                .name(name)
                .description("Test Description")
                .vegetarian(vegetarian)
                .servings(servings)
                .instructions("Test Instructions")
                .preparationTime(15)
                .cookingTime(30)
                .createdBy("testuser")
                .build();
        return recipeRepository.save(recipe);
    }
    
    private void addIngredientToRecipe(Recipe recipe, String name, String amount, String unit) {
        Ingredient ingredient = Ingredient.builder()
                .name(name)
                .amount(amount)
                .unit(unit)
                .build();
        recipe.addIngredient(ingredient);
    }
}