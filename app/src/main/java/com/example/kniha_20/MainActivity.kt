@file:OptIn(ExperimentalPageCurlApi::class)

package com.example.kniha_20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kniha_20.ui.screen.EditorRoute
import com.example.kniha_20.ui.screen.HomeRoute
import com.example.kniha_20.ui.screen.HomeScreen
import com.example.kniha_20.ui.screen.PlayerRoute
import com.example.kniha_20.ui.theme.Kniha_20Theme
import eu.wewox.pagecurl.ExperimentalPageCurlApi
import eu.wewox.pagecurl.config.rememberPageCurlConfig
import eu.wewox.pagecurl.page.PageCurl
import eu.wewox.pagecurl.page.rememberPageCurlState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kniha_20Theme {
                // Hlavní vstupní bod aplikace s navigací
                KnihaApp()
            }
        }
    }
}

/**
 * Hlavní kompozice, která drží Navigaci (Controller).
 * Rozhoduje, která obrazovka se právě zobrazí.
 */
@Composable
fun KnihaApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {

        // 1. DOMOVSKÁ STRÁNKA
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToPlayer = { navController.navigate(PlayerRoute) },
                onNavigateToEditor = { navController.navigate(EditorRoute) }
            )
        }

        // 2. PŘEHRÁVAČ ALBA (Zatím hardcodované demo)
        composable<PlayerRoute> {
            // Zde později načteme data z JSONu a pošleme je do přehrávače
            BookSpreadByClick()
        }

        // 3. EDITOR ALBA (Zatím jen text)
        composable<EditorRoute> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Zde budeme programovat Editor...")
            }
        }
    }
}

/**
 * Původní implementace přehrávače knihy (Demo).
 * Funguje na principu klikání vlevo/vpravo.
 */
@Composable
fun BookSpreadByClick() {
    val scope = rememberCoroutineScope()

    // Demo data: 12 stránek = 6 dvojstran (spreads)
    val totalPages = 12
    val spreads = (totalPages + 1) / 2

    // Stav otáčení stránek
    val curl = rememberPageCurlState()

    // Konfigurace: Vypneme tažení prstem, chceme jen klikání
    val config = rememberPageCurlConfig(
        tapForwardEnabled = false,
        tapBackwardEnabled = false,
        dragForwardEnabled = false,
        dragBackwardEnabled = false
    )

    Box(Modifier.fillMaxSize()) {
        // Samotná komponenta knihy
        PageCurl(
            count = spreads,
            state = curl,
            config = config
        ) { spreadIndex ->
            // Vypočítáme čísla stránek na aktuální dvojstraně
            val leftPage = spreadIndex * 2 + 1
            val rightPage = leftPage + 1

            Row(Modifier.fillMaxSize()) {
                // --- LEVÁ STRANA ---
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    PageFace(
                        label = "Strana $leftPage",
                        hint = "Klepni vlevo pro zpět"
                    )
                }

                // --- HŘBET KNIHY (Stín) ---
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                )

                // --- PRAVÁ STRANA ---
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (rightPage <= totalPages) {
                        PageFace(
                            label = "Strana $rightPage",
                            hint = "Klepni vpravo pro vpřed"
                        )
                    } else {
                        // Konec knihy (prázdná stránka)
                        PageFace(label = "", hint = "")
                    }
                }
            }
        }

        // --- OVLÁDACÍ VRSTVA (Overlay) ---
        // Neviditelná vrstva přes celou obrazovku, která zachytává kliknutí
        Row(Modifier.matchParentSize()) {
            // Levá polovina -> Zpět
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (curl.current > 0) {
                                scope.launch { curl.prev() }
                            }
                        }
                    }
            )
            // Pravá polovina -> Vpřed
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(spreads) {
                        detectTapGestures {
                            if (curl.current < spreads - 1) {
                                scope.launch { curl.next() }
                            }
                        }
                    }
            )
        }
    }
}

/**
 * Pomocná komponenta pro obsah stránky (texty uprostřed)
 */
@Composable
private fun PageFace(label: String, hint: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (label.isNotEmpty()) {
                Text(label, fontSize = 22.sp)
            }
            if (hint.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(hint, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun PreviewKnihaApp() {
    Kniha_20Theme {
        KnihaApp()
    }
}