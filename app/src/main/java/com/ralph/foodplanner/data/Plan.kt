package com.ralph.foodplanner.data

import androidx.room.Entity

@Entity(tableName = "plans", primaryKeys = ["name"])
data class Plan(
    val name: String,              // Planname als Primärschlüssel
    val meatCount: Int,            // Anzahl an Meat-Rezepten
    val vegetableCount: Int,       // Anzahl an Vegetable-Rezepten
    val fishCount: Int,            // Anzahl an Fish-Rezepten
    val dailyPortions: String,     // Portionsangaben für Montag bis Freitag, z.B. "10,12,8,9,11"
    val assignedRecipes: String    // Rezeptzuordnung, z.B. "Monday:recipeId1,recipeId2;Tuesday:recipeId3,recipeId4;..."
)
