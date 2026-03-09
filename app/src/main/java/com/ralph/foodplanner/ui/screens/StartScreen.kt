package com.ralph.foodplanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import com.ralph.foodplanner.R

@Composable
fun StartScreen(onGetStarted: () -> Unit) {
    // Automatisch nach 3 Sekunden zum nächsten Screen navigieren
    LaunchedEffect(Unit) {
        delay(1000)
        onGetStarted()
    }

    // Zeige ein Logo – hier als Platzhaltertext; ersetze es durch ein Image, falls vorhanden.
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo), //logo ist in res/drawable
            contentDescription = "App Logo"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen(onGetStarted = {})
}
