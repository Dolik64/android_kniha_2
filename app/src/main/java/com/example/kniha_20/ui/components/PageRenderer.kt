package com.example.kniha_20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kniha_20.data.model.AudioDecorator
import com.example.kniha_20.data.model.BackgroundDecorator
import com.example.kniha_20.data.model.BookComponent
import com.example.kniha_20.data.model.EmptyThing
import com.example.kniha_20.data.model.GridLayout
import com.example.kniha_20.data.model.ImagerThing
import com.example.kniha_20.data.model.InsetDecorator
import com.example.kniha_20.data.model.OpacityDecorator
import com.example.kniha_20.data.model.SplitLayout
import com.example.kniha_20.data.model.TextThing
import com.example.kniha_20.data.model.VideoThing

/**
 * REKURZIVNÍ RENDERER
 *
 * @param component Komponenta k vykreslení (z modelu)
 * @param modifier Modifikátory vzhledu
 * @param onImageClick Callback funkce, která se zavolá při kliknutí na obrázek.
 * Vrací ID komponenty (String).
 */
@Composable
fun RenderComponent(
    component: BookComponent,
    modifier: Modifier = Modifier,
    onImageClick: ((String) -> Unit)? = null
) {
    when (component) {
        // --- 1. LAYOUTY (Rozložení) ---
        // Musíme předat onImageClick dál do vnořených funkcí
        is GridLayout -> RenderGrid(component, modifier, onImageClick)
        is SplitLayout -> RenderSplit(component, modifier, onImageClick)

        // --- 2. DEKORÁTORY (Obaly) ---
        is InsetDecorator -> {
            val opt = component.options
            Box(
                modifier = modifier.padding(
                    start = opt?.left?.dp ?: 0.dp,
                    top = opt?.top?.dp ?: 0.dp,
                    end = opt?.right?.dp ?: 0.dp,
                    bottom = opt?.bottom?.dp ?: 0.dp
                )
            ) {
                // Rekurze: vykreslíme vnitřek a pošleme callback dál
                component.slots?.content?.let {
                    RenderComponent(it, onImageClick = onImageClick)
                }
            }
        }
        is BackgroundDecorator -> {
            Box(modifier = modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
                component.slots?.content?.let {
                    RenderComponent(it, onImageClick = onImageClick)
                }
            }
        }
        is OpacityDecorator -> {
            // Zde by se nastavila alpha, zatím jen propustíme obsah
            component.slots?.content?.let {
                RenderComponent(it, onImageClick = onImageClick)
            }
        }
        is AudioDecorator -> {
            component.slots?.content?.let {
                RenderComponent(it, onImageClick = onImageClick)
            }
        }

        // --- 3. OBSAH (Listy stromu) ---
        is ImagerThing -> RenderImage(component, modifier, onImageClick)
        is TextThing -> RenderText(component, modifier)
        is VideoThing -> {
            Box(modifier.background(Color.Black), contentAlignment = Alignment.Center) {
                Text("VIDEO: ${component.options?.uri ?: "No URI"}", color = Color.White)
            }
        }
        is EmptyThing -> {
            Spacer(modifier = modifier)
        }
    }
}

// --- POMOCNÉ FUNKCE PRO JEDNOTLIVÉ TYPY ---

@Composable
fun RenderGrid(
    component: GridLayout,
    modifier: Modifier,
    onImageClick: ((String) -> Unit)?
) {
    val rows = component.options?.rows ?: 1
    val cols = component.options?.columns ?: 1
    val gap = component.options?.gap?.dp ?: 0.dp
    val slots = component.slots

    Column(modifier = modifier.fillMaxSize()) {
        var itemIndex = 0
        for (r in 0 until rows) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                for (c in 0 until cols) {
                    val item = slots.getOrNull(itemIndex)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (item != null) {
                            // DŮLEŽITÉ: Předáváme callback dál
                            RenderComponent(item, onImageClick = onImageClick)
                        }
                    }
                    itemIndex++
                }
            }
            if (r < rows - 1) {
                Spacer(Modifier.height(gap))
            }
        }
    }
}

@Composable
fun RenderSplit(
    component: SplitLayout,
    modifier: Modifier,
    onImageClick: ((String) -> Unit)?
) {
    val isHorizontal = component.options?.horizontal == true
    val ratio = (component.options?.ratio ?: 50) / 100f
    val gap = component.options?.gap?.dp ?: 0.dp
    val first = component.slots?.first
    val second = component.slots?.second

    if (isHorizontal) {
        Row(modifier = modifier.fillMaxSize()) {
            Box(Modifier.weight(ratio).fillMaxHeight()) {
                if (first != null) RenderComponent(first, onImageClick = onImageClick)
            }
            Spacer(Modifier.width(gap))
            Box(Modifier.weight(1f - ratio).fillMaxHeight()) {
                if (second != null) RenderComponent(second, onImageClick = onImageClick)
            }
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            Box(Modifier.weight(ratio).fillMaxWidth()) {
                if (first != null) RenderComponent(first, onImageClick = onImageClick)
            }
            Spacer(Modifier.height(gap))
            Box(Modifier.weight(1f - ratio).fillMaxWidth()) {
                if (second != null) RenderComponent(second, onImageClick = onImageClick)
            }
        }
    }
}

@Composable
fun RenderImage(
    component: ImagerThing,
    modifier: Modifier,
    onImageClick: ((String) -> Unit)?
) {
    val url = component.options?.url ?: ""

    val contentScale = when (component.options?.size) {
        "cover" -> ContentScale.Crop
        "contain" -> ContentScale.Fit
        else -> ContentScale.Fit
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = "Obrázek",
        contentScale = contentScale,
        modifier = modifier
            .fillMaxSize()
            // ZDE JE KLÍČOVÁ ČÁST:
            // Pokud je definován callback (jsme v editoru), přidáme reakci na kliknutí.
            // Pokud je onImageClick null (jsme v přehrávači), obrázek nebude reagovat.
            .then(
                if (onImageClick != null) {
                    Modifier.clickable { onImageClick(component.id) }
                } else {
                    Modifier
                }
            )
    )
}

@Composable
fun RenderText(component: TextThing, modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = component.options?.html?.replace(Regex("<.*?>"), "") ?: "",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}