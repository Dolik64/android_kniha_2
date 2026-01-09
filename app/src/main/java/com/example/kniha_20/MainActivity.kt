@file:OptIn(ExperimentalPageCurlApi::class)

package com.example.kniha_20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kniha_20.model.*
import com.example.kniha_20.ui.components.RenderComponent
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
                KnihaApp()
            }
        }
    }
}

/**
 * Hlavní rozcestník (Navigace)
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

        // 2. PŘEHRÁVAČ
        composable<PlayerRoute> {
            BookSpreadByClick()
        }

        // 3. EDITOR (Test vykreslování z ASSETS)
        composable<EditorRoute> {
            // --- ZMĚNA ZDE ---
            // Načteme data z MockData (které odkazují na tvé obrázky v assets)
            val testPageData = MockData.getTestAlbum()

            // Vykreslíme stránku pomocí Rendererů
            Box(Modifier.fillMaxSize().background(Color.White)) {
                RenderComponent(component = testPageData)

                // Tlačítko zpět
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text("Zpět")
                }
            }
        }
    }
}

/**
 * Logika přehrávače knihy (PageCurl)
 */
@Composable
fun BookSpreadByClick() {
    val scope = rememberCoroutineScope()
    val totalPages = 12
    val spreads = (totalPages + 1) / 2
    val curl = rememberPageCurlState()

    // Konfigurace: Vypnutá gesta tažení, zapnutá jen na klik přes overlay
    val config = rememberPageCurlConfig(
        tapForwardEnabled = false,
        tapBackwardEnabled = false,
        dragForwardEnabled = false,
        dragBackwardEnabled = false
    )

    Box(Modifier.fillMaxSize()) {
        PageCurl(
            count = spreads,
            state = curl,
            config = config
        ) { spreadIndex ->
            val leftPage = spreadIndex * 2 + 1
            val rightPage = leftPage + 1

            Row(Modifier.fillMaxSize()) {
                // Levá stránka
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    PageFace(label = "Strana $leftPage", hint = "Klepni vlevo pro zpět")
                }

                // Hřbet
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                )

                // Pravá stránka
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (rightPage <= totalPages) {
                        PageFace(label = "Strana $rightPage", hint = "Klepni vpravo pro vpřed")
                    } else {
                        PageFace(label = "", hint = "")
                    }
                }
            }
        }

        // Overlay pro klikání (otáčení stránek)
        Row(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (curl.current > 0) scope.launch { curl.prev() }
                        }
                    }
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(spreads) {
                        detectTapGestures {
                            if (curl.current < spreads - 1) scope.launch { curl.next() }
                        }
                    }
            )
        }
    }
}

/**
 * Pomocná komponenta pro text na stránce v přehrávači
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