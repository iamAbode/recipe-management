package com.recipe.recipeservice.service;

import com.recipe.recipeservice.dto.RecipeDto;
import com.recipe.recipeservice.dto.RecipeFilterDto;
import com.recipe.recipeservice.entity.Ingredient;
import com.recipe.recipeservice.entity.Recipe;
import com.recipe.recipeservice.exception.RecipeNotFoundException;
import com.recipe.recipeservice.repository.RecipeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final Counter recipeCreatedCounter;
    private final Counter recipeUpdatedCounter;
    private final Counter recipeDeletedCounter;
    private final Timer recipeFilterTimer;
    private final MeterRegistry meterRegistry;

    public RecipeService(
            RecipeRepository recipeRepository, 
            RecipeMapper recipeMapper,
            Counter recipeCreatedCounter,
            Counter recipeUpdatedCounter,
            Counter recipeDeletedCounter,
            Timer recipeFilterTimer,
            MeterRegistry meterRegistry) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.recipeCreatedCounter = recipeCreatedCounter;
        this.recipeUpdatedCounter = recipeUpdatedCounter;
        this.recipeDeletedCounter = recipeDeletedCounter;
        this.recipeFilterTimer = recipeFilterTimer;
        this.meterRegistry = meterRegistry;
    }

    public List<RecipeDto> getAllRecipes() {
        meterRegistry.counter("recipe.queries", "type", "all").increment();
        return recipeRepository.findAll().stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }

    public RecipeDto getRecipeById(Long id) {
        meterRegistry.counter("recipe.queries", "type", "byId").increment();
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));
        return recipeMapper.toDto(recipe);
    }

    @Transactional
    public RecipeDto createRecipe(RecipeDto recipeDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        recipeDto.setCreatedBy(username);
        Recipe recipe = recipeMapper.toEntity(recipeDto);
        Recipe savedRecipe = recipeRepository.save(recipe);
        
        // Increment counter
        recipeCreatedCounter.increment();
        meterRegistry.gauge("recipe.count", recipeRepository.count());
        
        return recipeMapper.toDto(savedRecipe);
    }

    @Transactional
    public RecipeDto updateRecipe(Long id, RecipeDto recipeDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));

        // Check if the user is the owner of the recipe
        if (!existingRecipe.getCreatedBy().equals(username) && 
                !hasAdminRole()) {
            throw new AccessDeniedException("You are not authorized to update this recipe");
        }

        // Keep the original creator
        recipeDto.setCreatedBy(existingRecipe.getCreatedBy());
        recipeDto.setId(id);

        // Clear existing ingredients
        existingRecipe.getIngredients().clear();

        // Map the DTO to entity and save
        Recipe updatedRecipe = recipeMapper.toEntity(recipeDto);
        Recipe savedRecipe = recipeRepository.save(updatedRecipe);
        
        // Increment counter
        recipeUpdatedCounter.increment();
        
        return recipeMapper.toDto(savedRecipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));

        // Check if the user is the owner of the recipe
        if (!recipe.getCreatedBy().equals(username) && 
                !hasAdminRole()) {
            throw new AccessDeniedException("You are not authorized to delete this recipe");
        }

        recipeRepository.delete(recipe);
        
        // Increment counter
        recipeDeletedCounter.increment();
        meterRegistry.gauge("recipe.count", recipeRepository.count());
    }

    public List<RecipeDto> filterRecipes(RecipeFilterDto filterDto) {
        // Use timer to measure filter operation duration
        return recipeFilterTimer.record(() -> {
            // Check if no filter criteria are provided
            if (isEmptyFilter(filterDto)) {
                return getAllRecipes();
            }

            Set<Recipe> filteredRecipes = new HashSet<>();
            boolean isFirstFilter = true;

            // Track filter usage
            if (filterDto.getVegetarian() != null) {
                meterRegistry.counter("recipe.filter", "type", "vegetarian").increment();
            }
            if (filterDto.getServings() != null) {
                meterRegistry.counter("recipe.filter", "type", "servings").increment();
            }
            if (filterDto.getIncludeIngredient() != null && !filterDto.getIncludeIngredient().trim().isEmpty()) {
                meterRegistry.counter("recipe.filter", "type", "includeIngredient").increment();
            }
            if (filterDto.getExcludeIngredient() != null && !filterDto.getExcludeIngredient().trim().isEmpty()) {
                meterRegistry.counter("recipe.filter", "type", "excludeIngredient").increment();
            }
            if (filterDto.getInstructionText() != null && !filterDto.getInstructionText().trim().isEmpty()) {
                meterRegistry.counter("recipe.filter", "type", "instructionText").increment();
            }

            // Filter by vegetarian
            if (filterDto.getVegetarian() != null) {
                filteredRecipes.addAll(recipeRepository.findByVegetarian(filterDto.getVegetarian()));
                isFirstFilter = false;
            }

            // Filter by servings
            if (filterDto.getServings() != null) {
                if (isFirstFilter) {
                    filteredRecipes.addAll(recipeRepository.findByServings(filterDto.getServings()));
                    isFirstFilter = false;
                } else {
                    filteredRecipes.retainAll(recipeRepository.findByServings(filterDto.getServings()));
                }
            }

            // Filter by included ingredient
            if (filterDto.getIncludeIngredient() != null && !filterDto.getIncludeIngredient().trim().isEmpty()) {
                if (isFirstFilter) {
                    filteredRecipes.addAll(recipeRepository.findByIngredientNameContaining(filterDto.getIncludeIngredient().trim()));
                    isFirstFilter = false;
                } else {
                    filteredRecipes.retainAll(recipeRepository.findByIngredientNameContaining(filterDto.getIncludeIngredient().trim()));
                }
            }

            // Filter by excluded ingredient
            if (filterDto.getExcludeIngredient() != null && !filterDto.getExcludeIngredient().trim().isEmpty()) {
                if (isFirstFilter) {
                    filteredRecipes.addAll(recipeRepository.findByIngredientNameNotContaining(filterDto.getExcludeIngredient().trim()));
                    isFirstFilter = false;
                } else {
                    filteredRecipes.retainAll(recipeRepository.findByIngredientNameNotContaining(filterDto.getExcludeIngredient().trim()));
                }
            }

            // Filter by instruction text
            if (filterDto.getInstructionText() != null && !filterDto.getInstructionText().trim().isEmpty()) {
                if (isFirstFilter) {
                    filteredRecipes.addAll(recipeRepository.findByInstructionsContaining(filterDto.getInstructionText().trim()));
                } else {
                    filteredRecipes.retainAll(recipeRepository.findByInstructionsContaining(filterDto.getInstructionText().trim()));
                }
            }

            return filteredRecipes.stream()
                    .map(recipeMapper::toDto)
                    .collect(Collectors.toList());
        });
    }

    private boolean isEmptyFilter(RecipeFilterDto filterDto) {
        return filterDto.getVegetarian() == null &&
                filterDto.getServings() == null &&
                (filterDto.getIncludeIngredient() == null || filterDto.getIncludeIngredient().trim().isEmpty()) &&
                (filterDto.getExcludeIngredient() == null || filterDto.getExcludeIngredient().trim().isEmpty()) &&
                (filterDto.getInstructionText() == null || filterDto.getInstructionText().trim().isEmpty());
    }

    private boolean hasAdminRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public List<RecipeDto> getMyRecipes() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        meterRegistry.counter("recipe.queries", "type", "myRecipes").increment();
        return recipeRepository.findByCreatedBy(username).stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
    }
}