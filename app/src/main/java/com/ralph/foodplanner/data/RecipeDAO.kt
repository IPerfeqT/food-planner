
package com.ralph.foodplanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Transaction
    @Query("SELECT * FROM recipes WHERE recipeId = :recipeId")
    suspend fun getRecipeWithIngredients(recipeId: String): RecipeWithIngredients?

    @Query("DELETE FROM recipes WHERE recipeId = :recipeId")
    suspend fun deleteRecipe(recipeId: String)

    // Neue Funktion, um alle Rezepte abzurufen
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<Recipe>

    @Query("SELECT * FROM recipes WHERE kindOfFood = :type")
    suspend fun getRecipesByType(type: String): List<Recipe>

}
