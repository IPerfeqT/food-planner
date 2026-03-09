package com.ralph.foodplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ralph.foodplanner.data.DatabaseProvider
import com.ralph.foodplanner.data.Plan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlanViewModel(application: Application) : AndroidViewModel(application) {
    private val planDao = com.ralph.foodplanner.data.DatabaseProvider.getPlanDatabase(application).planDao()

    fun insertPlan(plan: Plan) {
        viewModelScope.launch(Dispatchers.IO) {
            planDao.insertPlan(plan)
        }
    }

    suspend fun getPlan(name: String): Plan? {
        return planDao.getPlan(name)
    }

    suspend fun getAllPlans(): List<Plan> {
        return planDao.getAllPlans()
    }

    fun deletePlan(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            planDao.deletePlan(name)
        }
    }
}
