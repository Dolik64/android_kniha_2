package com.example.kniha_20.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kniha_20.R
import com.example.kniha_20.ui.components.NavImageButton
import com.example.kniha_20.ui.components.PageFace
import eu.wewox.pagecurl.ExperimentalPageCurlApi
import eu.wewox.pagecurl.config.rememberPageCurlConfig
import eu.wewox.pagecurl.page.PageCurl
import eu.wewox.pagecurl.page.rememberPageCurlState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPageCurlApi::class)
@Composable
fun PlayerScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val totalPages = 12
    val spreads = (totalPages + 1) / 2
    val curl = rememberPageCurlState()

    val config = rememberPageCurlConfig(
        tapForwardEnabled = false,
        tapBackwardEnabled = false,
        dragForwardEnabled = false,
        dragBackwardEnabled = false
    )

    Box(Modifier.fillMaxSize()) {
        PageCurl(count = spreads, state = curl, config = config) { spreadIndex ->
            val leftPage = spreadIndex * 2 + 1
            val rightPage = leftPage + 1
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    PageFace(label = "Strana $leftPage", hint = "")
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)))
                Box(Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                    if (rightPage <= totalPages) PageFace(label = "Strana $rightPage", hint = "")
                }
            }
        }

        // Tlačítko Zpět
        androidx.compose.material3.Button(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            androidx.compose.material3.Text("Menu")
        }

        if (curl.current > 0) {
            NavImageButton(resId = R.drawable.btn_prev, modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)) {
                scope.launch { curl.prev() }
            }
        }
        if (curl.current < spreads - 1) {
            NavImageButton(resId = R.drawable.btn_next, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                scope.launch { curl.next() }
            }
        }
    }
}