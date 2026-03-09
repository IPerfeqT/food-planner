# Food Planner

An Android meal planning app built with Kotlin and Jetpack Compose. 
Create weekly meal plans, manage your own recipe collection, and save 
plan presets — all stored locally with Room.

## Features
- Weekly meal plan view with a recipe assigned to each day
- Add custom recipes with ingredients, cooking time, portions, and instructions
- Categorize recipes by type (meat, fish, vegetarian, dessert, soup)
- Auto-generate weekly plans based on recipe type counts
- Save and load plan presets from a local database
- Search recipes when editing a day's meal
- Delete recipes and plans via the Settings screen
- Splash screen with app logo on launch

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Architecture:** MVVM (ViewModel + Repository pattern)
- **Database:** Room (two databases: recipes and plans)
- **Navigation:** Navigation Compose
- **Build:** Gradle with KSP for Room annotation processing

## Requirements
- Android Studio (Hedgehog or later)
- Android SDK 26+
- Gradle 8.10.2

## Setup
1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync
4. Run on an emulator or physical device (Android 8.0+)

## Notes
- The app ships with a preloaded recipe database (recipe_database.db)
- User-created recipes and plans are stored locally and persist across sessions
- No internet connection required
