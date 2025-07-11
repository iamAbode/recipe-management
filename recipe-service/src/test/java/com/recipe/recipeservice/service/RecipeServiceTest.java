package com.recipe.recipeservice.service;

import com.recipe.recipeservice.dto.IngredientDto;
import com.recipe.recipeservice.dto.RecipeDto;
import com.recipe.recipeservice.dto.RecipeFilterDto;
import com.recipe.recipeservice.entity.Ingredient;
import com.recipe.recipeservice.entity.Recipe;
import com.recipe.recipeservice.exception.RecipeNotFoundException;
import com.recipe.recipeservice.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe recipe;
    private RecipeDto recipeDto;

    @BeforeEach
    void setUp() {
        // Set up SecurityContext mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        // Set up test data
        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Test Ingredient")
                .amount("100")
                .unit("g")
                .build();

        recipe = Recipe.builder()
                .id(1L)
                .name("Test Recipe")
                .description("Test Description")
                .vegetarian(true)
                .servings(4)
                .instructions("Test Instructions")
                .preparationTime(15)
                .cookingTime(30)
                .createdBy("testuser")
                .build();
        recipe.addIngredient(ingredient);

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
    }

    @Test
    void getAllRecipes_ShouldReturnAllRecipes() {
        // Arrange
        when(recipeRepository.findAll()).thenReturn(Collections.singletonList(recipe));
        when(recipeMapper.toDto(recipe)).thenReturn(recipeDto);

        // Act
        List<RecipeDto> result = recipeService.getAllRecipes();

        // Assert
        assertEquals(1, result.size());
        assertEquals(recipeDto, result.get(0));
        verify(recipeRepository, times(1)).findAll();
    }

    @Test
    void getRecipeById_WithValidId_ShouldReturnRecipe() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toDto(recipe)).thenReturn(recipeDto);

        // Act
        RecipeDto result = recipeService.getRecipeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(recipeDto, result);
        verify(recipeRepository, times(1)).findById(1L);
    }

    @Test
    void getRecipeById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecipeNotFoundException.class, () -> {
            recipeService.getRecipeById(999L);
        });
        verify(recipeRepository, times(1)).findById(999L);
    }

    @Test
    void createRecipe_ShouldReturnCreatedRecipe() {
        // Arrange
        when(recipeMapper.toEntity(recipeDto)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toDto(recipe)).thenReturn(recipeDto);

        // Act
        RecipeDto result = recipeService.createRecipe(recipeDto);

        // Assert
        assertNotNull(result);
        assertEquals(recipeDto, result);
        assertEquals("testuser", recipeDto.getCreatedBy());
        verify(recipeRepository, times(1)).save(recipe);
    }

    @Test
    void updateRecipe_AsOwner_ShouldUpdateAndReturnRecipe() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toEntity(recipeDto)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toDto(recipe)).thenReturn(recipeDto);

        // Act
        RecipeDto result = recipeService.updateRecipe(1L, recipeDto);

        // Assert
        assertNotNull(result);
        assertEquals(recipeDto, result);
        verify(recipeRepository, times(1)).save(recipe);
    }

    @Test
    void updateRecipe_AsNonOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        Recipe otherUserRecipe = Recipe.builder()
                .id(1L)
                .name("Test Recipe")
                .createdBy("otheruser")
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(otherUserRecipe));
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            recipeService.updateRecipe(1L, recipeDto);
        });
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void updateRecipe_AsAdmin_ShouldUpdateAnyRecipe() {
        // Arrange
        Recipe otherUserRecipe = Recipe.builder()
                .id(1L)
                .name("Test Recipe")
                .createdBy("otheruser")
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(otherUserRecipe));
        when(authentication.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(recipeMapper.toEntity(recipeDto)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toDto(recipe)).thenReturn(recipeDto);

        // Act
        RecipeDto result = recipeService.updateRecipe(1L, recipeDto);

        // Assert
        assertNotNull(result);
        assertEquals(recipeDto, result);
        verify(recipeRepository, times(1)).save(recipe);
    }

    @Test
    void filterRecipes_WithVegetarianFilter_ShouldReturnVegetarianRecipes() {
        // Arrange
        RecipeFilterDto filterDto = new RecipeFilterDto();
        filterDto.setVegetarian(true);

        when(recipeRepository.findByVegetarian(true)).thenReturn(Collections.singletonList(recipe));
        when(recipeMapper.toDto(recipe)).thenReturn(recipeDto);

        // Act
        List<RecipeDto> result = recipeService.filterRecipes(filterDto);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getVegetarian());
        verify(recipeRepository, times(1)).findByVegetarian(true);
    }
}