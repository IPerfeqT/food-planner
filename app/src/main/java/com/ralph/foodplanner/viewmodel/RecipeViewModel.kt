
package com.ralph.foodplanner.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ralph.foodplanner.data.DatabaseProvider
import com.ralph.foodplanner.data.Ingredient
import com.ralph.foodplanner.data.Recipe
import com.ralph.foodplanner.data.RecipeWithIngredients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val recipeDao = DatabaseProvider.getRecipeDatabase(application).recipeDao()

    // Funktion, um ein Rezept mitsamt Zutaten abzurufen
    suspend fun getRecipeWithIngredients(recipeId: String): RecipeWithIngredients? {
        return recipeDao.getRecipeWithIngredients(recipeId)
    }

    suspend fun getAllRecipes(): List<Recipe> {
        return recipeDao.getAllRecipes()
    }

    // Löscht ein Rezept anhand des recipeId.
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.deleteRecipe(recipeId)
        }
    }

    suspend fun getRecipesByType(type: String): List<Recipe> {
        return recipeDao.getRecipesByType(type)
    }



    fun insertRecipeWithIngredients(
        recipe: Recipe,
        ingredients: List<Ingredient>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeDao.insertRecipe(recipe)
            recipeDao.insertIngredients(ingredients)
        }
    }

    fun printRecipe(recipeId: String) {
        viewModelScope.launch {
            val recipeWithIngredients = recipeDao.getRecipeWithIngredients(recipeId)
            Log.d("RecipeViewModel", "Recipe retrieved: $recipeWithIngredients")
        }
    }
}
