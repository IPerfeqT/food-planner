package com.ralph.foodplanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: Plan)

    @Query("SELECT * FROM plans WHERE name = :name")
    suspend fun getPlan(name: String): Plan?

    @Query("SELECT * FROM plans")
    suspend fun getAllPlans(): List<Plan>

    @Query("DELETE FROM plans WHERE name = :name")
    suspend fun deletePlan(name: String)
}
