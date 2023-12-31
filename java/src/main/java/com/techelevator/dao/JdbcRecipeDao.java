package com.techelevator.dao;

import com.techelevator.model.Ingredient;
import com.techelevator.model.Recipe;
import com.techelevator.model.RecipeIngredientListDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcRecipeDao implements RecipeDao{
    private final JdbcTemplate jdbcTemplate;

    public JdbcRecipeDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int addRecipe(Recipe recipeToAdd, int userId) {
        String sql = "INSERT INTO recipes (user_id, recipe_name, recipe_image, recipe_ingredients, instructions) VALUES (?, ?, ?, ?, ?) RETURNING recipe_id;";
        // TODO - add method that puts a row in recipe_ingredients, and call it here when adding a recipe.

        int recipeId;
        if (recipeToAdd.getImage() == null){
            recipeToAdd.setImage("");
        }
        recipeToAdd.setUserId(userId);
        try {
            // create recipe, but don't do anything with ingredients yet!
            recipeId = jdbcTemplate.queryForObject(sql, int.class, recipeToAdd.getUserId(), recipeToAdd.getName(), recipeToAdd.getImage(), recipeToAdd.getIngredients(), recipeToAdd.getInstructions());
            // once the recipe is created, add a row in the join table
            // TODO: Make specific exception types for DAO-related issues.
            // TODO: Think about what kind of exceptions we want to build.
        } catch (CannotGetJdbcConnectionException e) {
            throw new RuntimeException("Unable to connect to the database.", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Action would violate data integrity.", e);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Invalid syntax.", e);
        }

        return recipeId;
    }

    @Override
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT recipe_id, user_id, recipe_name, recipe_image, recipe_ingredients, instructions FROM recipes";
        // TODO: add a method that gets recipe ingredients from recipe_ingredients, call it here to build list of lists that represents a recipe's ingredients
        try {
            SqlRowSet rows = jdbcTemplate.queryForRowSet(sql);
            while(rows.next()){
                recipes.add(mapRowToRecipe(rows));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new RuntimeException("Unable to connect to the database.", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Action would violate data integrity.", e);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Invalid syntax.", e);
        }

        return recipes;
    }

    @Override
    public List<Recipe> getAllRecipesByUserId(int userId) {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT recipe_id, user_id, recipe_name, recipe_image, recipe_ingredients, instructions FROM recipes WHERE user_id = ?;";
        try {
            SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, userId);
            while (rows.next()){
                recipes.add(mapRowToRecipe(rows));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new RuntimeException("Unable to connect to the database.", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Action would violate data integrity.", e);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Invalid syntax.", e);
        }

        return recipes;

    }

    @Override
    public Recipe getRecipeById(int id) {
        Recipe recipe = null;
        String sql = "SELECT recipe_id, user_id, recipe_name, recipe_image, recipe_ingredients, instructions " +
                "FROM recipes " +
                "WHERE recipe_id = ?";
        try {
            SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, id);
            if (rows.next()) {
                recipe = mapRowToRecipe(rows);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new RuntimeException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Action would violate data integrity.", e);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Invalid syntax.", e);
        }

        return recipe;
    }

    @Override
    public Recipe updateRecipe(Recipe recipe) {
        Recipe updatedRecipe = null;
        String sql = "UPDATE recipes " +
                "SET recipe_name = ?, " +
                "recipe_image = ?, " +
                "recipe_ingredients = ?, " +
                "instructions = ? " +
                "WHERE recipe_id = ?";
        try {
            int numberOfRows = jdbcTemplate.update(sql, recipe.getName(), recipe.getImage(), recipe.getIngredients(), recipe.getInstructions(), recipe.getId());
            if (numberOfRows == 0) {
                throw new RuntimeException("Zero rows affected, expected at least one");
            } else {
                updatedRecipe = getRecipeById(recipe.getId());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new RuntimeException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Action would violate data integrity.", e);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Invalid syntax.", e);
        }

        return updatedRecipe;
    }
    public List<Recipe> getRecipesByMealId(int mealId){
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT recipe_id, user_id, recipe_name, recipe_image, instructions FROM recipes WHERE recipe_id IN (SELECT recipe_id FROM meal_recipes WHERE meal_id = ?)";
        try {
            SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, mealId);
            while (rows.next()){
                recipes.add(mapRowToRecipe(rows));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new RuntimeException("Unable to connect to the database.", e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Action would violate data integrity.", e);
        } catch (BadSqlGrammarException e) {
            throw new RuntimeException("Invalid syntax.", e);
        }


        return recipes;
    }





    public Recipe mapRowToRecipe(SqlRowSet rows){
        Recipe newRecipe = new Recipe();
        newRecipe.setId(rows.getInt("recipe_id"));
        newRecipe.setUserId(rows.getInt("user_id"));
        newRecipe.setName(rows.getString("recipe_name"));
        newRecipe.setImage(rows.getString("recipe_image"));
        newRecipe.setIngredients(rows.getString("recipe_ingredients"));
        //TODO: write the piece for setting the Ingredients List
        //newRecipe.setIngredients list isn't set here. I tried using getObject, but couldn't get it to work
        //Changed it to a string, but does it need to me a list or array?
        newRecipe.setInstructions(rows.getString("instructions"));
        return newRecipe;
    }
}
