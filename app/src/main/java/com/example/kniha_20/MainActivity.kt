
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
import eu.wewox.pagecurl.config.rememberPageCurlConfig





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

    val totalPages = 12
    val spreads = (totalPages + 1) / 2

    // jeden společný stav pro celou dvojstranu
    val curl = rememberPageCurlState()

    // vypneme interní gesta knihovny, kliky řeší overlay
    val config = rememberPageCurlConfig(
        tapForwardEnabled = false,
        tapBackwardEnabled = false,
        dragForwardEnabled = false,
        dragBackwardEnabled = false
    )

    Box(Modifier.fillMaxSize()) {
        // jedna PageCurl přes celou šířku, index = dvojstrana
        PageCurl(
            count = spreads,
            state = curl,
            config = config
        ) { spreadIndex ->
            val leftPage = spreadIndex * 2 + 1
            val rightPage = leftPage + 1

            Row(Modifier.fillMaxSize()) {
                // levá strana
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

                // hřbet
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                )

                // pravá strana
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
                        PageFace(label = "", hint = "")
                    }
                }
            }
        }

        // klikací overlay přes obě poloviny
        Row(Modifier.matchParentSize()) {
            // vlevo = prev
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
            // vpravo = next
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
