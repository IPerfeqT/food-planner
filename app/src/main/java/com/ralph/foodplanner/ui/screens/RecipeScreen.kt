package com.ralph.foodplanner.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ralph.foodplanner.data.Ingredient
import com.ralph.foodplanner.data.Recipe
import com.ralph.foodplanner.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeScreen(
    onBack: () -> Unit
) {
    // Zustände für die Recipe-Eingabefelder:
    var recipeName by remember { mutableStateOf("") }
    var portionAmount by remember { mutableStateOf("") }
    var timeToCook by remember { mutableStateOf("") }
    var variant by remember { mutableStateOf("") }
    var dishType by remember { mutableStateOf("") }
    val dishTypeOptions = listOf("meat", "vegetarian", "fish", "dessert", "soup") //add in still missing of dessert, soup and instead of vegetable add vegetarian
    var dropdownExpanded by remember { mutableStateOf(false) }
    var instructions by remember { mutableStateOf("") }

    // Zutatenliste – diese wird über die Zutatenverwaltung bearbeitet
    val ingredients = remember { mutableStateListOf<Ingredient>() }
    // Zustände für den Ingredient Dialog:
    var showIngredientsDialog by remember { mutableStateOf(false) }
    var editingIngredientIndex by remember { mutableStateOf(-1) }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQuantity by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("") }

    // Warn-Dialog, falls Pflichtfelder fehlen
    var showWarningDialog by remember { mutableStateOf(false) }

    // Hole das RecipeViewModel, um mit der Datenbank zu arbeiten
    val recipeViewModel: RecipeViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // BottomBar: Save-Button (rechts unten)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        // Validierung: Alle Pflichtfelder müssen gefüllt sein
                        if (recipeName.isBlank() ||
                            portionAmount.isBlank() ||
                            timeToCook.isBlank() ||
                            dishType.isBlank() ||
                            instructions.isBlank() ||
                            variant.isBlank() ||
                            ingredients.isEmpty()
                        ) {
                            showWarningDialog = true
                        } else {
                            // Erzeuge den zusammengesetzten Schlüssel (recipeId) aus recipeName und variant
                            val recipeId = "$recipeName#$variant"
                            // Erstelle das Recipe-Objekt:
                            val recipe = Recipe(
                                name = recipeName,
                                portion = portionAmount.toIntOrNull() ?: 0,
                                cookingTime = timeToCook.toIntOrNull() ?: 0,
                                kindOfFood = dishType,
                                instructions = instructions,
                                variant = variant,
                                recipeId = recipeId
                            )
                            // Aktualisiere jede Zutat, sodass sie den recipeId erhält:
                            val updatedIngredients = ingredients.map { ingredient ->
                                // Hier verwenden wir die Ingredient-Klasse aus dem Data-Package
                                // WICHTIG: Erzeuge eine separate ingredientId für jede Zutat
                                val ingredientId = "$recipeId#${ingredient.ingredientName}"
                                com.ralph.foodplanner.data.Ingredient(
                                    recipeId = recipeId,
                                    ingredientName = ingredient.ingredientName,
                                    quantity = ingredient.quantity,
                                    unit = ingredient.unit,
                                    ingredientId = ingredientId
                                )
                            }
                            // Speichere Rezept und Zutaten in der Datenbank über das ViewModel
                            recipeViewModel.insertRecipeWithIngredients(recipe, updatedIngredients)

                            onBack()
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Überschrift
            Text("New Recipe", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            // Layout in zwei Spalten: Links für Recipe Details, rechts für Zutatenliste
            Row(modifier = Modifier.fillMaxWidth()) {
                // Linke Spalte: Recipe Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        label = { Text("Recipe Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = portionAmount,
                        onValueChange = { portionAmount = it },
                        label = { Text("Portion Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = timeToCook,
                        onValueChange = { timeToCook = it },
                        label = { Text("Time to cook (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = variant,
                        onValueChange = { variant = it },
                        label = { Text("Variant (e.g. Default)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = dishType,
                            onValueChange = { /* read-only */ },
                            readOnly = true,
                            label = { Text("Select dish type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            dishTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        dishType = option
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                // Rechte Spalte: Zutatenliste
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        itemsIndexed(ingredients) { index, ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { /* Optionale Aktion */ },
                                        onLongClick = {
                                            editingIngredientIndex = index
                                            ingredientName = ingredient.ingredientName
                                            ingredientQuantity = ingredient.quantity
                                            ingredientUnit = ingredient.unit
                                            showIngredientsDialog = true
                                        }
                                    )
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${ingredient.quantity} ${ingredient.unit} ${ingredient.ingredientName}")
                            }
                            Divider()
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        IconButton(onClick = {
                            editingIngredientIndex = -1
                            ingredientName = ""
                            ingredientQuantity = ""
                            ingredientUnit = ""
                            showIngredientsDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Ingredient"
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }

    // Dialog zum Hinzufügen/Bearbeiten von Zutaten
    if (showIngredientsDialog) {
        AlertDialog(
            onDismissRequest = { showIngredientsDialog = false },
            title = { Text(if (editingIngredientIndex >= 0) "Edit Ingredient" else "Add Ingredient") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ingredientName,
                        onValueChange = { ingredientName = it },
                        label = { Text("Ingredient Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ingredientQuantity,
                        onValueChange = { ingredientQuantity = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ingredientUnit,
                        onValueChange = { ingredientUnit = it },
                        label = { Text("Unit (e.g., gram, TS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (ingredientName.isNotBlank() && ingredientQuantity.isNotBlank() && ingredientUnit.isNotBlank()) {
                        // Erzeuge ein neues Ingredient-Objekt. Der recipeId wird erst beim Speichern gesetzt.
                        val newIngredient = com.ralph.foodplanner.data.Ingredient(
                            recipeId = "", // Temporär leer; wird später im Save-Button aktualisiert
                            ingredientName = ingredientName,
                            quantity = ingredientQuantity,
                            unit = ingredientUnit,
                            ingredientId = "" // temporär leer wird später aktualisiert
                        )
                        if (editingIngredientIndex >= 0) {
                            ingredients[editingIngredientIndex] = newIngredient
                        } else {
                            ingredients.add(newIngredient)
                        }
                        showIngredientsDialog = false
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIngredientsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Warn-Dialog, falls nicht alle Pflichtfelder gefüllt sind
    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text("Warning") },
            text = { Text("Not all fields are filled!") },
            confirmButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChoiceButtonRecipe(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PortionCounterRowRecipe(
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
                .padding(8.dp)
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
fun RecipeScreenPreview() {
    RecipeScreen(onBack = { })
}
