
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
import com.example.kniha_20.ui.theme.Kniha_20Theme
import eu.wewox.pagecurl.page.PageCurl
import eu.wewox.pagecurl.page.rememberPageCurlState
import eu.wewox.pagecurl.ExperimentalPageCurlApi




import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Kniha_20Theme { BookSpreadByClick() } }
    }
}

/**
 * Otevřená kniha: dvě stránky vedle sebe.
 * Klik vlevo = předchozí dvojstrana, klik vpravo = další dvojstrana.
 */
@Composable
fun BookSpreadByClick() {
    val scope = rememberCoroutineScope()

    // počet “papírových” stran (levá = 1, pravá = 2, atd.)
    val totalPages = 12

    // index levé strany aktuální dvojstrany; držíme ho vždy sudý: 0,2,4…
    var spreadStart by remember { mutableStateOf(0) }

    val leftCurl = rememberPageCurlState()
    val rightCurl = rememberPageCurlState()

    Row(Modifier.fillMaxSize()) {
        // LEVÁ STRANA
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Jednoduché 2-frame “curl” plátno; obsah měníme podle spreadStart
            PageCurl(count = 2, state = leftCurl) { _ ->
                PageFace(
                    label = "Strana ${spreadStart + 1}",
                    hint = "Klepni vlevo pro zpět"
                )
            }

            // klikací plocha – levou půlku obrazovky používáme pro návrat
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(spreadStart) {
                        detectTapGestures {
                            if (spreadStart > 0) {
                                scope.launch { leftCurl.prev() }
                                spreadStart = (spreadStart - 2).coerceAtLeast(0)
                            }
                        }
                    }
            )
        }

        // DĚLICÍ ČÁRA HŘBETU KNIHY
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
        )

        // PRAVÁ STRANA
        val rightIndex = spreadStart + 1
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            PageCurl(count = 2, state = rightCurl) { _ ->
                if (rightIndex < totalPages) {
                    PageFace(
                        label = "Strana ${rightIndex + 1}",
                        hint = "Klepni vpravo pro vpřed"
                    )
                } else {
                    // lichý počet stran: prázdná poslední pravá
                    PageFace(label = "", hint = "")
                }
            }

            // klikací plocha – pravá půlka jde vpřed
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(spreadStart, totalPages) {
                        detectTapGestures {
                            val nextStart = spreadStart + 2
                            if (nextStart < totalPages) {
                                scope.launch { rightCurl.next() }
                                spreadStart = nextStart
                            }
                        }
                    }
            )
        }
    }
}

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
fun PreviewBookSpreadByClick() {
    Kniha_20Theme { BookSpreadByClick() }
}
