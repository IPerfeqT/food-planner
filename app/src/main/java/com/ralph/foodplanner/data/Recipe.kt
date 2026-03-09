package com.ralph.foodplanner.data

import androidx.room.Entity

@Entity(tableName = "recipes", primaryKeys = ["recipeId"])
data class Recipe(
    val name: String,
    val portion: Int,
    val cookingTime: Int,     // in Minuten
    val kindOfFood: String,   // z. B. "meat", "vegetables", "fish"
    val instructions: String,
    val variant: String,      // z. B. "Standard", "Variant 1"
    val recipeId: String      // Zusammengesetzt aus name und variant, z. B. "Spaghetti#Standard"
)
