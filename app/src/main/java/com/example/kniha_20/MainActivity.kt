package com.example.kniha_20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.kniha_20.ui.theme.Kniha_20Theme
import com.eschao.android.widget.pageflip.PageFlip

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Kniha_20Theme { PageFlipSmoke() } }
    }
}

@Composable
fun PageFlipSmoke() {
    val context = LocalContext.current
    var msg by remember { mutableStateOf("PageFlip inicializace…") }

    LaunchedEffect(Unit) {
        val result = runCatching {
            val pf = PageFlip(context)
            pf.enableAutoPage(true)
            pf.enableClickToFlip(true)
            pf.javaClass.name
        }
        msg = result.fold(
            onSuccess = { "PageFlip import OK: $it" },
            onFailure = { "PageFlip chyba: ${it.message ?: it::class.java.simpleName}" }
        )
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(msg)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPageFlipSmoke() {
    Kniha_20Theme { PageFlipSmoke() }
}
