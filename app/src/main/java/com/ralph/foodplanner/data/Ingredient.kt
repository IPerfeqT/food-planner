
package com.ralph.foodplanner.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredients",
    primaryKeys = ["ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["recipeId"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Ingredient(
    val recipeId: String,      // Muss mit Recipe.recipeId übereinstimmen
    val ingredientName: String,
    val quantity: String,      // z. B. "400"
    val unit: String,          // z. B. "gram" oder "TS"
    val ingredientId: String = "$recipeId#$ingredientName"
)

