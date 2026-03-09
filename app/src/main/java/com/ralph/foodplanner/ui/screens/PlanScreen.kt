package com.ralph.foodplanner.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ralph.foodplanner.data.Plan
import com.ralph.foodplanner.data.Recipe
import com.ralph.foodplanner.viewmodel.PlanViewModel
import com.ralph.foodplanner.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

// Dummy-Funktionen als Fallback
fun getDummyRecipe(dish: String): String {
    return when (dish) {
        "Spaghetti" -> "Boil pasta, add sauce, sprinkle cheese."
        "Salad" -> "Mix greens with dressing and toppings."
        "Pizza" -> "Bake dough with tomato sauce, cheese and toppings."
        "Suppe" -> "Simmer ingredients until tender."
        "Burger" -> "Grill patty, assemble with bun and condiments."
        "Sushi" -> "Roll rice with fish and vegetables."
        "Braten" -> "Roast meat with herbs and vegetables."
        else -> "Default recipe instructions."
    }
}

fun getDummyIngredients(dish: String): List<String> {
    return when (dish) {
        "Spaghetti" -> listOf("Pasta", "Tomato Sauce", "Cheese")
        "Salad" -> listOf("Lettuce", "Tomatoes", "Cucumber")
        "Pizza" -> listOf("Dough", "Tomato Sauce", "Cheese", "Pepperoni")
        "Suppe" -> listOf("Broth", "Carrots", "Celery", "Onion")
        "Burger" -> listOf("Bun", "Patty", "Lettuce", "Cheese")
        "Sushi" -> listOf("Rice", "Fish", "Seaweed")
        "Braten" -> listOf("Meat", "Spices", "Vegetables")
        else -> listOf("Ingredient X", "Ingredient Y")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(navController: NavController) {
    val context = LocalContext.current

    // Initialer Wochenplan: Tag und Standard-Rezept (als recipeId: "Dish#default")
    val initialWeekPlan = listOf(
        "Montag" to "Spaghetti",
        "Dienstag" to "Salad",
        "Mittwoch" to "Pizza",
        "Donnerstag" to "Suppe",
        "Freitag" to "Burger",
        "Samstag" to "Sushi",
        "Sonntag" to "Braten"
    )

    // SharedPreferences zum Speichern des Wochenplans
    val sharedPreferences = context.getSharedPreferences("weekly_plan", Context.MODE_PRIVATE)

    // Prüfe, ob ein Plan-Update durchgeführt wurde
    val planUpdated = sharedPreferences.getBoolean("plan_updated", false)

    // Lade den Wochenplan aus SharedPreferences oder verwende Standardwerte:
    val loadedWeekPlan = initialWeekPlan.map { (day, dish) ->
        val defaultRecipeId = "$dish#default"
        val savedRecipeId = sharedPreferences.getString("week_plan_$day", defaultRecipeId) ?: defaultRecipeId
        day to savedRecipeId
    }

    // Zustand für den Wochenplan als mutable Liste (Pair: Tag, recipeId)
    val weekPlanState = remember { mutableStateListOf<Pair<String, String>>() }

    // Initialisiere den Wochenplan beim ersten Laden oder wenn ein Update durchgeführt wurde
    LaunchedEffect(planUpdated) {
        if (weekPlanState.isEmpty() || planUpdated) {
            weekPlanState.clear()
            weekPlanState.addAll(loadedWeekPlan)
            // Reset den Update-Flag
            if (planUpdated) {
                sharedPreferences.edit().putBoolean("plan_updated", false).apply()
            }
        }
    }

    // Zustand: Welcher Tag (Index) wurde ausgewählt?
    var selectedDishIndex by remember { mutableStateOf(0) }
    // Speichere den aktuell verwendeten recipeId (initial aus weekPlanState)
    var currentRecipeId by remember { mutableStateOf(weekPlanState.getOrNull(selectedDishIndex)?.second ?: "") }
    // Zustände für die geladenen Rezeptdaten:
    var selectedRecipeText by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf(listOf<String>()) }

    // Dialogzustände
    var showRecipeDialog by remember { mutableStateOf(false) }
    var showIngredientsDialog by remember { mutableStateOf(false) }
    var showRecipeListDialog by remember { mutableStateOf(false) }
    var showPlanOptionsDialog by remember { mutableStateOf(false) }
    var showPlanSelectionDialog by remember { mutableStateOf(false) }
    var recipeList by remember { mutableStateOf(emptyList<Recipe>()) }
    var planList by remember { mutableStateOf(emptyList<Plan>()) }

    // Hole das RecipeViewModel und einen CoroutineScope
    val recipeViewModel: RecipeViewModel = viewModel()
    val planViewModel: PlanViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // Funktion zum Laden von zufälligen Rezepten basierend auf Typen (meat, vegetables, fish)
    fun loadRandomRecipesByType(meatCount: Int, vegetablesCount: Int, fishCount: Int) {
        coroutineScope.launch {
            // Hole Rezepte aus der DB für jeden Typ // es fehlen noch rezepttypen
            val meatRecipes = recipeViewModel.getRecipesByType("meat")
            val vegRecipes = recipeViewModel.getRecipesByType("vegetables")
            val fishRecipes = recipeViewModel.getRecipesByType("fish")

            // Für jeden Tag (Monday bis Sunday) wähle zufällig die angegebene Anzahl Rezepte:
            for ((index, pair) in weekPlanState.withIndex()) {
                val day = pair.first
                val dayRecipes = mutableListOf<String>()

                // Zufällig Meat-Rezepte auswählen
                if (meatRecipes.isNotEmpty() && meatCount > 0) {
                    val selectedMeat = meatRecipes.shuffled().take(meatCount)
                    selectedMeat.forEach { recipe ->
                        dayRecipes.add(recipe.recipeId)
                    }
                }

                // Zufällig Vegetable-Rezepte auswählen
                if (vegRecipes.isNotEmpty() && vegetablesCount > 0) {
                    val selectedVeg = vegRecipes.shuffled().take(vegetablesCount)
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

                // Update des Wochenplans mit dem ersten Rezept
                if (dayRecipes.isNotEmpty()) {
                    weekPlanState[index] = day to dayRecipes.first()
                    // Speichere in SharedPreferences
                    sharedPreferences.edit().putString("week_plan_$day", dayRecipes.first()).apply()

                    // Speichere alle Rezepte für den Tag
                    val allRecipes = dayRecipes.joinToString(",")
                    sharedPreferences.edit().putString("week_plan_${day}_all", allRecipes).apply()
                }
            }

            // Update der Anzeige für den aktuell ausgewählten Tag
            if (weekPlanState.isNotEmpty() && selectedDishIndex < weekPlanState.size) {
                currentRecipeId = weekPlanState[selectedDishIndex].second
            }
        }
    }

    // LaunchedEffect: Wenn sich selectedDishIndex oder currentRecipeId ändert, lade Rezeptdaten aus der DB.
    LaunchedEffect(selectedDishIndex, currentRecipeId) {
        if (currentRecipeId.isNotEmpty()) {
            val recipeWithIngredients = recipeViewModel.getRecipeWithIngredients(currentRecipeId)
            if (recipeWithIngredients != null) {
                selectedRecipeText = recipeWithIngredients.recipe.instructions
                selectedIngredients = recipeWithIngredients.ingredients.map { ingredient: com.ralph.foodplanner.data.Ingredient ->
                    "${ingredient.quantity} ${ingredient.unit} ${ingredient.ingredientName}"
                }
            } else {
                // Fallback: Nutze Dummy-Daten
                val dish = currentRecipeId.substringBefore("#")
                selectedRecipeText = getDummyRecipe(dish)
                selectedIngredients = getDummyIngredients(dish)
            }
        }
    }

    // Speichere weekPlanState in SharedPreferences, wenn sich dieser ändert
    LaunchedEffect(weekPlanState.size) {
        weekPlanState.forEach { (day, recipeId) ->
            sharedPreferences.edit().putString("week_plan_$day", recipeId).apply()
        }
    }

    // Popup: Plan-Auswahlsdialog
    if (showPlanSelectionDialog) {
        Dialog(onDismissRequest = { showPlanSelectionDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text("Select a Saved Plan", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (planList.isEmpty()) {
                        Text("No saved plans found.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn {
                            items(planList) { plan ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Extrahiere Werte aus dem Plan
                                            val meatCount = plan.meatCount
                                            val vegCount = plan.vegetableCount
                                            val fishCount = plan.fishCount

                                            // Lade Rezepte basierend auf den Typen
                                            loadRandomRecipesByType(meatCount, vegCount, fishCount)

                                            // Speichere als aktiven Plan in SharedPreferences
                                            val activePlanPrefs = context.getSharedPreferences("active_plan", Context.MODE_PRIVATE)
                                            activePlanPrefs.edit().putString("active_plan", "planName:${plan.name};meat:${plan.meatCount};veg:${plan.vegetableCount};fish:${plan.fishCount};portions:${plan.dailyPortions}").apply()

                                            showPlanSelectionDialog = false
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    Column {
                                        Text(plan.name, style = MaterialTheme.typography.titleMedium)
                                        Text("Meat: ${plan.meatCount}, Veg: ${plan.vegetableCount}, Fish: ${plan.fishCount}",
                                            style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Divider()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showPlanSelectionDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Popup: Optionsdialog (Neuer Plan etc.)
    if (showPlanOptionsDialog) {
        Dialog(onDismissRequest = { showPlanOptionsDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text("Select an Option", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            coroutineScope.launch {
                                planList = planViewModel.getAllPlans()
                                showPlanOptionsDialog = false
                                showPlanSelectionDialog = true
                            }
                        }) {
                            Text("Choose Existing Plan")
                        }
                        Button(onClick = {
                            showPlanOptionsDialog = false
                            navController.navigate("planCreation")
                        }) {
                            Text("Create New Plan")
                        }
                    }
                }
            }
        }
    }

    // Popup: Rezeptdetail-Dialog
    if (showRecipeDialog) {
        Dialog(onDismissRequest = { showRecipeDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text("Recipe Detail", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedRecipeText, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showRecipeDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Popup: Zutaten-Dialog
    if (showIngredientsDialog) {
        Dialog(onDismissRequest = { showIngredientsDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text("Ingredients", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    for (ingredient in selectedIngredients) {
                        Text(ingredient, style = MaterialTheme.typography.bodyMedium)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showIngredientsDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Popup: Rezeptlisten-Dialog (Edit-Funktion mit Suchleiste)
    if (showRecipeListDialog) {
        RecipeListDialog(
            recipes = recipeList,
            onDismiss = { showRecipeListDialog = false },
            onRecipeSelected = { selectedRecipe ->
                currentRecipeId = selectedRecipe.recipeId
                // Optional: Update des Wochenplans für den ausgewählten Tag
                val day = weekPlanState[selectedDishIndex].first
                weekPlanState[selectedDishIndex] = day to selectedRecipe.recipeId
                // Speichere in SharedPreferences:
                sharedPreferences.edit().putString("week_plan_$day", selectedRecipe.recipeId).apply()
                // Lade Rezeptdaten aus der DB
                coroutineScope.launch {
                    val recipeWithIngredients = recipeViewModel.getRecipeWithIngredients(selectedRecipe.recipeId)
                    if (recipeWithIngredients != null) {
                        selectedRecipeText = recipeWithIngredients.recipe.instructions
                        selectedIngredients = recipeWithIngredients.ingredients.map { ingredient: com.ralph.foodplanner.data.Ingredient ->
                            "${ingredient.quantity} ${ingredient.unit} ${ingredient.ingredientName}"
                        }
                    }
                }
                showRecipeListDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Plan") },
                actions = {
                    IconButton(onClick = { /* Einstellungen */navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { showPlanOptionsDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Neuer Plan")
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(8.dp)
        ) {
            // Linke Spalte: Wochenplan und Edit-Button
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Card(
                    modifier = Modifier.wrapContentHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn {
                        itemsIndexed(weekPlanState) { index, pair ->
                            val (day, recipeId) = pair
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDishIndex = index
                                        currentRecipeId = recipeId
                                    }
                            ) {
                                // Zeige als Anzeige den Tag und den "Dish" (nur den Teil vor dem "#")
                                WeekPlanItem(day = day, dish = recipeId.substringBefore("#"))
                            }
                            if (index < weekPlanState.size - 1) {
                                Divider()
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            recipeList = recipeViewModel.getAllRecipes()
                        }
                        showRecipeListDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit")
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Rechte Spalte: Zutaten-View, Rezeptdetail und Add-Button
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { showIngredientsDialog = true },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState())
                    ) {
                        Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        for (ingredient in selectedIngredients) {
                            Text(ingredient, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clickable { showRecipeDialog = true },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(8.dp)
                    ) {
                        Text("Selected Recipe", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(selectedRecipeText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("addRecipe") }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun WeekPlanItem(day: String, dish: String) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = day, style = MaterialTheme.typography.titleMedium)
        Text(text = dish, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RecipeListDialog(
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onRecipeSelected: (Recipe) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val filteredRecipes = recipes.filter { it.name.contains(searchText, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Recipe") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search recipe") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(filteredRecipes) { recipe ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecipeSelected(recipe) }
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PlanScreenPreview() {
    PlanScreen(navController = androidx.navigation.compose.rememberNavController())
}