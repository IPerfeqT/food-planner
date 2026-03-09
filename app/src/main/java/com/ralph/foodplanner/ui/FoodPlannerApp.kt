package com.ralph.foodplanner.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ralph.foodplanner.ui.screens.PlanCreationScreen
import com.ralph.foodplanner.ui.screens.PlanScreen
import com.ralph.foodplanner.ui.screens.StartScreen
import com.ralph.foodplanner.ui.screens.RecipeScreen
import com.ralph.foodplanner.ui.screens.SettingsScreen

@Composable
fun FoodPlannerApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartScreen(onGetStarted = {
                navController.navigate("plan")
            })
        }
        composable("plan") {
            // Wir übergeben hier den NavController an den PlanScreen,
            // damit dieser innerhalb des Screens navigieren kann.
            PlanScreen(navController = navController)
        }
        composable("planCreation") {
            PlanCreationScreen(
                onCancel = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }
        composable("addRecipe") {
            // Hier wird der RecipeScreen aufgerufen
            RecipeScreen(onBack =  {navController.popBackStack()}) // Du kannst hier später auch Parameter (z.B. onBack) übergeben
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                onBack = {navController.popBackStack() }
            )
        }
    }
}
