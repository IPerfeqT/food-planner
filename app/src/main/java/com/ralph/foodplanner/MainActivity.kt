package com.ralph.foodplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ralph.foodplanner.ui.FoodPlannerApp
import com.ralph.foodplanner.ui.screens.StartScreen
import com.ralph.foodplanner.ui.theme.FoodPlannerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodPlannerTheme {
                FoodPlannerApp()
            }
        }
    }
}