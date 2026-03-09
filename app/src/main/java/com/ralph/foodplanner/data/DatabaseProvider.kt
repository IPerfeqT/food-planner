package com.ralph.foodplanner.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var recipeDatabase: RecipeDatabase? = null

    @Volatile
    private var planDatabase: PlanDatabase? = null

    fun getRecipeDatabase(context: Context): RecipeDatabase {
        return recipeDatabase ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                RecipeDatabase::class.java,
                "recipe_database"
            )
                .createFromAsset("recipe_database.db")
                .build()
            recipeDatabase = instance
            instance
        }
    }


    fun getPlanDatabase(context: Context): PlanDatabase {
        return planDatabase ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                PlanDatabase::class.java,
                "plan_database"
            ).build()
            planDatabase = instance
            instance
        }
    }
}
