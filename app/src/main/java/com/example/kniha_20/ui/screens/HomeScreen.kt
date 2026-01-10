package com.example.kniha_20.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
        onNavigateToPlayer: () -> Unit,
onNavigateToEditor: () -> Unit
) {
Scaffold { padding ->
        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
    Text(
            text = "Moje Knihovna",
            style = MaterialTheme.typography.headlineLarge
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Tlačítko pro přehrání (simulace výběru alba)
    Button(onClick = onNavigateToPlayer) {
        Text("Přehrát demo album")
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tlačítko pro editor
    Button(onClick = onNavigateToEditor) {
        Text("Vytvořit nové album")
    }
}
}
}