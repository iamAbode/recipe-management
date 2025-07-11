package com.recipe.recipeservice.repository;

import com.recipe.recipeservice.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByVegetarian(boolean vegetarian);

    List<Recipe> findByServings(int servings);

    @Query("SELECT r FROM Recipe r JOIN r.ingredients i WHERE i.name LIKE %:ingredient%")
    List<Recipe> findByIngredientNameContaining(@Param("ingredient") String ingredient);

    @Query("SELECT r FROM Recipe r JOIN r.ingredients i WHERE i.name NOT LIKE %:ingredient%")
    List<Recipe> findByIngredientNameNotContaining(@Param("ingredient") String ingredient);

    @Query("SELECT r FROM Recipe r WHERE r.instructions LIKE %:text%")
    List<Recipe> findByInstructionsContaining(@Param("text") String text);

    @Query("SELECT r FROM Recipe r WHERE r.vegetarian = :vegetarian AND r.servings = :servings")
    List<Recipe> findByVegetarianAndServings(@Param("vegetarian") boolean vegetarian, @Param("servings") int servings);

    @Query("SELECT r FROM Recipe r JOIN r.ingredients i WHERE r.vegetarian = :vegetarian AND i.name LIKE %:ingredient%")
    List<Recipe> findByVegetarianAndIngredient(@Param("vegetarian") boolean vegetarian, @Param("ingredient") String ingredient);

    @Query("SELECT r FROM Recipe r JOIN r.ingredients i WHERE r.servings = :servings AND i.name LIKE %:ingredient%")
    List<Recipe> findByServingsAndIngredient(@Param("servings") int servings, @Param("ingredient") String ingredient);

    @Query("SELECT r FROM Recipe r WHERE r.instructions LIKE %:text% AND r.vegetarian = :vegetarian")
    List<Recipe> findByInstructionsContainingAndVegetarian(@Param("text") String text, @Param("vegetarian") boolean vegetarian);

    List<Recipe> findByCreatedBy(String username);
}