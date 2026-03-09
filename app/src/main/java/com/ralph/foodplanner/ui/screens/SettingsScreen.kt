package com.ralph.foodplanner.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ralph.foodplanner.data.Plan
import com.ralph.foodplanner.data.Recipe
import com.ralph.foodplanner.viewmodel.PlanViewModel
import com.ralph.foodplanner.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    // Für Rezepte
    val recipeViewModel: RecipeViewModel = viewModel()
    // Für Pläne
    val planViewModel: PlanViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    // SnackbarHostState (manuell erstellt)
    val snackbarHostState = remember { SnackbarHostState() }

    // Zustände für Rezept-Löschung
    var recipeList by remember { mutableStateOf(emptyList<Recipe>()) }
    var showRecipeListDialog by remember { mutableStateOf(false) }
    var showRecipeConfirmDialog by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    // Zustände für Plan-Löschung
    var planList by remember { mutableStateOf(emptyList<Plan>()) }
    var showPlanListDialog by remember { mutableStateOf(false) }
    var showPlanConfirmDialog by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Button zum Löschen von Rezepten
            Button(
                onClick = {
                    coroutineScope.launch {
                        recipeList = recipeViewModel.getAllRecipes()
                    }
                    showRecipeListDialog = true
                }
            ) {
                Text("Delete Recipe")
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Button zum Löschen von Plänen
            Button(
                onClick = {
                    coroutineScope.launch {
                        planList = planViewModel.getAllPlans()
                    }
                    showPlanListDialog = true
                }
            ) {
                Text("Delete Plan")
            }
        }

        // Rezeptlisten-Dialog (für das Löschen von Rezepten)
        if (showRecipeListDialog) {
            AlertDialog(
                onDismissRequest = { showRecipeListDialog = false },
                title = { Text("Select Recipe to Delete") },
                text = {
                    Column {
                        LazyColumn {
                            items(recipeList) { recipe ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRecipe = recipe
                                            showRecipeListDialog = false
                                            showRecipeConfirmDialog = true
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${recipe.name} (${recipe.variant})")
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showRecipeListDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Bestätigungs-Dialog für Rezept-Löschung
        if (showRecipeConfirmDialog && selectedRecipe != null) {
            AlertDialog(
                onDismissRequest = { showRecipeConfirmDialog = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Do you want to delete recipe: ${selectedRecipe!!.name} (${selectedRecipe!!.variant})?") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            recipeViewModel.deleteRecipe(selectedRecipe!!.recipeId)
                            snackbarHostState.showSnackbar("Recipe deleted successfully.")
                        }
                        showRecipeConfirmDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRecipeConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Planlisten-Dialog (für das Löschen von Plänen)
        if (showPlanListDialog) {
            AlertDialog(
                onDismissRequest = { showPlanListDialog = false },
                title = { Text("Select Plan to Delete") },
                text = {
                    Column {
                        LazyColumn {
                            items(planList) { plan ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedPlan = plan
                                            showPlanListDialog = false
                                            showPlanConfirmDialog = true
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(plan.name)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPlanListDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Bestätigungs-Dialog für Plan-Löschung
        if (showPlanConfirmDialog && selectedPlan != null) {
            AlertDialog(
                onDismissRequest = { showPlanConfirmDialog = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Do you want to delete plan: ${selectedPlan!!.name}?") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            planViewModel.deletePlan(selectedPlan!!.name)
                            snackbarHostState.showSnackbar("Plan deleted successfully.")
                        }
                        showPlanConfirmDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPlanConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
