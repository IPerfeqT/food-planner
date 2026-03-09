// Updated PlanCreationScreen.kt
package com.ralph.foodplanner.ui.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ralph.foodplanner.data.Plan
import com.ralph.foodplanner.viewmodel.PlanViewModel
import com.ralph.foodplanner.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCreationScreen(
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("active_plan", Context.MODE_PRIVATE)
    // Lade den aktiven Plan (als String) aus SharedPreferences
    var activePlanJson by remember { mutableStateOf(sharedPreferences.getString("active_plan", "") ?: "") }

    // Eingabefeld für den Plan-Namen (wird im Hauptinhalt angezeigt)
    var planName by remember { mutableStateOf("") }

    // Rezeptzählungen für jeden Typ – Anzahl der Rezepte, die pro Tag zugeordnet werden sollen
    var meatCount by remember { mutableStateOf(0) }
    var vegetableCount by remember { mutableStateOf(0) }
    var fishCount by remember { mutableStateOf(0) }

    // Portionsangaben für alle 7 Tage (kompakte Darstellung)
    val weekDays = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    val dailyPortions = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0) }

    // Checkbox: Save as Preset – wenn aktiviert, wird der Plan zusätzlich in der Datenbank gespeichert
    var saveToDatabase by remember { mutableStateOf(false) }

    // ViewModels
    val planViewModel: PlanViewModel = viewModel()
    val recipeViewModel: RecipeViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // SharedPreferences für den Wochenplan
    val weekPlanPrefs = context.getSharedPreferences("weekly_plan", Context.MODE_PRIVATE)

    Scaffold(
        topBar = {
            // TopAppBar mit statischem Titel "Create New Plan" (neben dem ArrowBack)
            TopAppBar(
                title = { Text("Create New Plan") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // BottomBar: Save-Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Hole Rezepte aus der DB für jeden Typ
                            val meatRecipes = recipeViewModel.getRecipesByType("meat")
                            val vegRecipes = recipeViewModel.getRecipesByType("vegetables")
                            val fishRecipes = recipeViewModel.getRecipesByType("fish")

                            // Für jeden Tag (Monday bis Sunday) wähle zufällig die angegebene Anzahl Rezepte:
                            for ((index, day) in weekDays.withIndex()) {
                                val dayRecipes = mutableListOf<String>()

                                // Zufällig Meat-Rezepte auswählen
                                if (meatRecipes.isNotEmpty() && meatCount > 0) {
                                    val selectedMeat = meatRecipes.shuffled().take(meatCount)
                                    selectedMeat.forEach { recipe ->
                                        dayRecipes.add(recipe.recipeId)
                                    }
                                }

                                // Zufällig Vegetable-Rezepte auswählen
                                if (vegRecipes.isNotEmpty() && vegetableCount > 0) {
                                    val selectedVeg = vegRecipes.shuffled().take(vegetableCount)
                                    selectedVeg.forEach { recipe ->
                                        dayRecipes.add(recipe.recipeId)
                                    }
                                }

                                // Zufällig Fish-Rezepte auswählen
                                if (fishRecipes.isNotEmpty() && fishCount > 0) {
                                    val selectedFish = fishRecipes.shuffled().take(fishCount)
                                    selectedFish.forEach { recipe ->
                                        dayRecipes.add(recipe.recipeId)
                                    }
                                }

                                // Falls keine Rezepte gefunden wurden, verwende Default-Rezept
                                if (dayRecipes.isEmpty()) {
                                    val defaultDish = when (index) {
                                        0 -> "Spaghetti"
                                        1 -> "Salad"
                                        2 -> "Pizza"
                                        3 -> "Suppe"
                                        4 -> "Burger"
                                        5 -> "Sushi"
                                        6 -> "Braten"
                                        else -> "Default"
                                    }
                                    dayRecipes.add("$defaultDish#default")
                                }

                                // Speichere erstes Rezept für den Tag im Wochenplan
                                weekPlanPrefs.edit().putString("week_plan_$day", dayRecipes.first()).apply()

                                // Speichere alle Rezepte für den Tag (könnte später genutzt werden)
                                val allRecipes = dayRecipes.joinToString(",")
                                weekPlanPrefs.edit().putString("week_plan_${day}_all", allRecipes).apply()
                            }

                            // Speichere den aktiven Plan in SharedPreferences
                            val dailyPortionsStr = dailyPortions.joinToString(separator = ",")
                            val planJson = "planName:$planName;meat:$meatCount;veg:$vegetableCount;fish:$fishCount;portions:$dailyPortionsStr"
                            sharedPreferences.edit().putString("active_plan", planJson).apply()

                            // Speichere den Plan in der Datenbank wenn gewünscht
                            if (saveToDatabase && planName.isNotBlank()) {
                                val assignedRecipesStr = weekDays.mapIndexed { index, day ->
                                    val recipes = weekPlanPrefs.getString("week_plan_${day}_all", "") ?: ""
                                    "$day:$recipes"
                                }.joinToString(";")

                                val plan = Plan(
                                    name = planName,
                                    meatCount = meatCount,
                                    vegetableCount = vegetableCount,
                                    fishCount = fishCount,
                                    dailyPortions = dailyPortionsStr,
                                    assignedRecipes = assignedRecipesStr
                                )
                                planViewModel.insertPlan(plan)
                            }
                        }

                        // Trigger update in PlanScreen
                        weekPlanPrefs.edit().putBoolean("plan_updated", true).apply()

                        onCreate() // Navigiere zurück (z. B. zum PlanScreen)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    ) { innerPadding ->
        // Hauptinhalt: Eingabefelder für den Plan
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Eingabefeld für den Plan-Namen (wird im Hauptteil zusätzlich angezeigt)
            OutlinedTextField(
                value = planName,
                onValueChange = { planName = it },
                label = { Text("Enter Plan Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Active Plan Preview
            Text("Active Plan Preview", style = MaterialTheme.typography.headlineSmall)
            Text(activePlanJson.ifBlank { "No active plan" }, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Eingabefelder für die Rezeptzählungen (für Meat, Vegetables, Fish)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedTextField(
                    value = meatCount.toString(),
                    onValueChange = { newVal -> meatCount = newVal.toIntOrNull() ?: 0 },
                    label = { Text("Meat Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = vegetableCount.toString(),
                    onValueChange = { newVal -> vegetableCount = newVal.toIntOrNull() ?: 0 },
                    label = { Text("Vegetable Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = fishCount.toString(),
                    onValueChange = { newVal -> fishCount = newVal.toIntOrNull() ?: 0 },
                    label = { Text("Fish Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Kompakte Darstellung der Portionsangaben für alle 7 Tage
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Daily Portions (Monday to Sunday)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                for (i in weekDays.indices) {
                    DayPortionInput(
                        day = weekDays[i],
                        portion = dailyPortions[i],
                        onIncrease = { dailyPortions[i]++ },
                        onDecrease = { if (dailyPortions[i] > 0) dailyPortions[i]-- }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox: Save as Preset
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = saveToDatabase,
                    onCheckedChange = { saveToDatabase = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save as Preset", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DayPortionInput(
    day: String,
    portion: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = day, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onDecrease) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = "Decrease")
        }
        Text(
            text = portion.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onIncrease) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChoiceButton(
    count: Int,
    onShortClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .combinedClickable(
                    onClick = onShortClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Choice Button"
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Pressed: $count", style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PortionCounterRow(
    day: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(text = day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(40.dp)
                .combinedClickable(
                    onClick = onIncrement,
                    onLongClick = onDecrement
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("$count", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanCreationScreenPreview() {
    PlanCreationScreen(
        onCancel = { /* Cancel-Aktion */ },
        onCreate = { /* Navigation zurück zum PlanScreen */ }
    )
}