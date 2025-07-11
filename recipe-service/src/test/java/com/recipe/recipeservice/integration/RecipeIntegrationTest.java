package com.recipe.recipeservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.recipeservice.dto.IngredientDto;
import com.recipe.recipeservice.dto.RecipeDto;
import com.recipe.recipeservice.entity.Recipe;
import com.recipe.recipeservice.repository.RecipeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RecipeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecipeRepository recipeRepository;

    @BeforeEach
    void setUp() {
        // Set up security context with a test user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "testuser",
                        "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                ));
    }

    @AfterEach
    void tearDown() {
        recipeRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAndRetrieveRecipe() throws Exception {
        // Create a recipe DTO for testing
        IngredientDto ingredientDto = IngredientDto.builder()
                .name("Potato")
                .amount("500")
                .unit("g")
                .build();

        RecipeDto recipeDto = RecipeDto.builder()
                .name("Test Recipe")
                .description("A test recipe")
                .vegetarian(true)
                .servings(4)
                .instructions("Cook for 30 minutes")
                .preparationTime(15)
                .cookingTime(30)
                .ingredients(Collections.singletonList(ingredientDto))
                .build();

        // Create the recipe via API
        String responseJson = mockMvc.perform(post("/recipes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recipeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Recipe"))
                .andExpect(jsonPath("$.vegetarian").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RecipeDto createdRecipe = objectMapper.readValue(responseJson, RecipeDto.class);
        Long recipeId = createdRecipe.getId();

        // Verify the recipe was saved in the database
        Recipe savedRecipe = recipeRepository.findById(recipeId).orElse(null);
        assertNotNull(savedRecipe);
        assertEquals("Test Recipe", savedRecipe.getName());
        assertEquals(true, savedRecipe.isVegetarian());
        assertEquals(1, savedRecipe.getIngredients().size());
        assertEquals("Potato", savedRecipe.getIngredients().get(0).getName());

        // Retrieve the recipe via API
        mockMvc.perform(get("/recipes/" + recipeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recipeId))
                .andExpect(jsonPath("$.name").value("Test Recipe"))
                .andExpect(jsonPath("$.ingredients[0].name").value("Potato"));
    }

    @Test
    void filterRecipesByVegetarian() throws Exception {
        // Create a vegetarian recipe
        IngredientDto vegetableDto = IngredientDto.builder()
                .name("Broccoli")
                .amount("300")
                .unit("g")
                .build();

        RecipeDto vegetarianRecipeDto = RecipeDto.builder()
                .name("Vegetarian Recipe")
                .description("A vegetarian recipe")
                .vegetarian(true)
                .servings(2)
                .instructions("Steam the broccoli")
                .ingredients(Collections.singletonList(vegetableDto))
                .build();

        // Create a non-vegetarian recipe
        IngredientDto meatDto = IngredientDto.builder()
                .name("Chicken")
                .amount("500")
                .unit("g")
                .build();

        RecipeDto nonVegetarianRecipeDto = RecipeDto.builder()
                .name("Non-Vegetarian Recipe")
                .description("A non-vegetarian recipe")
                .vegetarian(false)
                .servings(4)
                .instructions("Cook the chicken")
                .ingredients(Collections.singletonList(meatDto))
                .build();

        // Save both recipes via API
        mockMvc.perform(post("/recipes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vegetarianRecipeDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/recipes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonVegetarianRecipeDto)))
                .andExpect(status().isCreated());

        // Filter recipes by vegetarian = true
        mockMvc.perform(post("/recipes/filter")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonMap("vegetarian", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Vegetarian Recipe"))
                .andExpect(jsonPath("$[0].vegetarian").value(true))
                .andExpect(jsonPath("$[1]").doesNotExist()); // Only one result should be returned
    }
}