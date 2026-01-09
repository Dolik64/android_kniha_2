@file:OptIn(ExperimentalPageCurlApi::class)

package com.example.kniha_20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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

        // 2. PŘEHRÁVAČ (KNIHA)
        composable<PlayerRoute> {
            BookSpreadByClick()
        }

        // 3. EDITOR (Test vykreslování)
        composable<EditorRoute> {
            // Vytvoříme testovací strukturu stránky pro editor
            val testPageData = BackgroundDecorator(
                slots = ContentSlot(
                    content = InsetDecorator(
                        options = InsetOptions(top = 40.0, left = 20.0, right = 20.0, bottom = 40.0),
                        slots = ContentSlot(
                            content = GridLayout(
                                options = GridOptions(rows = 2, columns = 1, gap = 10),
                                slots = listOf(
                                    ImagerThing(options = ImageOptions(url = "file:///android_asset/foto1.jpg")),
                                    TextThing(options = TextOptions(html = "Ukázka z editoru"))
                                )
                            )
                        )
                    )
                )
            )

            Box(Modifier.fillMaxSize().background(Color.White)) {
                RenderComponent(component = testPageData)
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
 * Logika přehrávače knihy s TLAČÍTKY
 */
@Composable
fun BookSpreadByClick() {
    val scope = rememberCoroutineScope()
    val totalPages = 12 // Celkový počet stran (může být dynamický z modelu)
    val spreads = (totalPages + 1) / 2
    val curl = rememberPageCurlState()

    // 1. DŮLEŽITÉ: Konfigurace - Vypneme všechna gesta (klikání i tažení na samotné stránce)
    // To zajistí, že stránka se otočí POUZE přes naše tlačítka.
    val config = rememberPageCurlConfig(
        tapForwardEnabled = false,
        tapBackwardEnabled = false,
        dragForwardEnabled = false,
        dragBackwardEnabled = false
    )

    Box(Modifier.fillMaxSize()) {
        // A. Samotná kniha (vrstva vespod)
        PageCurl(
            count = spreads,
            state = curl,
            config = config
        ) { spreadIndex ->
            // Výpočet čísel stránek na dvojstraně
            val leftPage = spreadIndex * 2 + 1
            val rightPage = leftPage + 1

            Row(Modifier.fillMaxSize()) {
                // Levá strana
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // Zde by se volal RenderComponent(leftPageModel)
                    PageFace(label = "Strana $leftPage", hint = "")
                }

                // Stín hřbetu knihy uprostřed
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                )

                // Pravá strana
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (rightPage <= totalPages) {
                        // Zde by se volal RenderComponent(rightPageModel)
                        PageFace(label = "Strana $rightPage", hint = "")
                    }
                }
            }
        }

        // B. Vrstva s tlačítky (vrstva nahoře - Overlay)

        // Tlačítko ZPĚT (zobrazí se, pokud nejsme na úplném začátku)
        if (curl.current > 0) {
            NavImageButton(
                resId = R.drawable.btn_prev, // Ujistěte se, že tento soubor existuje v res/drawable
                modifier = Modifier
                    .align(Alignment.CenterStart) // Zarovnat vlevo doprostřed
                    .padding(start = 16.dp)       // Odsazení od kraje
            ) {
                scope.launch { curl.prev() }
            }
        }

        // Tlačítko VPŘED (zobrazí se, pokud nejsme na úplném konci)
        if (curl.current < spreads - 1) {
            NavImageButton(
                resId = R.drawable.btn_next, // Ujistěte se, že tento soubor existuje v res/drawable
                modifier = Modifier
                    .align(Alignment.CenterEnd)   // Zarovnat vpravo doprostřed
                    .padding(end = 16.dp)         // Odsazení od kraje
            ) {
                scope.launch { curl.next() }
            }
        }
    }
}

/**
 * Pomocná komponenta pro obrázkové tlačítko s reakcí na kliknutí (Ripple effect)
 */
@Composable
fun NavImageButton(
    resId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // IconButton zajistí "ripple" efekt (kruhové vlnění při stisku)
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(130.dp) // Velikost tlačítka - upravte dle potřeby
    ) {
        // Vykreslení PNG obrázku uvnitř tlačítka
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Navigace",
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Pomocná komponenta pro text na stránce v přehrávači (Demo obsah)
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