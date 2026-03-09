
package com.ralph.foodplanner.data

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    val ingredients: List<Ingredient>
)
