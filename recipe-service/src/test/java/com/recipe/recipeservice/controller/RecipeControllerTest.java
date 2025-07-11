package com.recipe.recipeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.recipeservice.dto.IngredientDto;
import com.recipe.recipeservice.dto.RecipeDto;
import com.recipe.recipeservice.dto.RecipeFilterDto;
import com.recipe.recipeservice.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@WithMockUser
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipeService recipeService;

    private RecipeDto recipeDto;
    private List<RecipeDto> recipeDtos;

    @BeforeEach
    void setUp() {
        IngredientDto ingredientDto = IngredientDto.builder()
                .id(1L)
                .name("Test Ingredient")
                .amount("100")
                .unit("g")
                .build();

        recipeDto = RecipeDto.builder()
                .id(1L)
                .name("Test Recipe")
                .description("Test Description")
                .vegetarian(true)
                .servings(4)
                .instructions("Test Instructions")
                .preparationTime(15)
                .cookingTime(30)
                .ingredients(Collections.singletonList(ingredientDto))
                .createdBy("testuser")
                .build();

        recipeDtos = Collections.singletonList(recipeDto);
    }

    @Test
    void getAllRecipes_ShouldReturnAllRecipes() throws Exception {
        // Arrange
        when(recipeService.getAllRecipes()).thenReturn(recipeDtos);

        // Act & Assert
        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Recipe"));
    }

    @Test
    void getRecipeById_ShouldReturnRecipe() throws Exception {
        // Arrange
        when(recipeService.getRecipeById(1L)).thenReturn(recipeDto);

        // Act & Assert
        mockMvc.perform(get("/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Recipe"));
    }

    @Test
    void createRecipe_ShouldReturnCreatedRecipe() throws Exception {
        // Arrange
        when(recipeService.createRecipe(any(RecipeDto.class))).thenReturn(recipeDto);

        // Act & Assert
        mockMvc.perform(post("/recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Recipe"));
    }

    @Test
    void updateRecipe_ShouldReturnUpdatedRecipe() throws Exception {
        // Arrange
        when(recipeService.updateRecipe(eq(1L), any(RecipeDto.class))).thenReturn(recipeDto);

        // Act & Assert
        mockMvc.perform(put("/recipes/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Recipe"));
    }

    @Test
    void deleteRecipe_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/recipes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void filterRecipes_ShouldReturnFilteredRecipes() throws Exception {
        // Arrange
        RecipeFilterDto filterDto = new RecipeFilterDto();
        filterDto.setVegetarian(true);

        when(recipeService.filterRecipes(any(RecipeFilterDto.class))).thenReturn(recipeDtos);

        // Act & Assert
        mockMvc.perform(post("/recipes/filter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].vegetarian").value(true));
    }
}