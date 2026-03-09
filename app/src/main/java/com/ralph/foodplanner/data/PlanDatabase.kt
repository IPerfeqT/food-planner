package com.ralph.foodplanner.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Plan::class], version = 1, exportSchema = false)
abstract class PlanDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
}
