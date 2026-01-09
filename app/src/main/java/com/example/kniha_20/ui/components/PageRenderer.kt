package com.example.kniha_20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kniha_20.model.*

/**
 * REKURZIVNÍ RENDERER
 * Tato funkce se podívá, co je to za komponentu, a podle toho ji vykreslí.
 * Pokud komponenta obsahuje další děti (slots), zavolá se znovu pro ně.
 */
@Composable
fun RenderComponent(component: BookComponent, modifier: Modifier = Modifier) {
    when (component) {
        // --- 1. LAYOUTY (Rozložení) ---
        is GridLayout -> RenderGrid(component, modifier)
        is SplitLayout -> RenderSplit(component, modifier)

        // --- 2. DEKORÁTORY (Obaly) ---
        is InsetDecorator -> {
            // Inset přidá padding a pak vykreslí vnitřek
            val opt = component.options
            Box(
                modifier = modifier.padding(
                    start = opt?.left?.dp ?: 0.dp,
                    top = opt?.top?.dp ?: 0.dp,
                    end = opt?.right?.dp ?: 0.dp,
                    bottom = opt?.bottom?.dp ?: 0.dp
                )
            ) {
                // Pokud má obsah, vykreslí ho
                component.slots?.content?.let { RenderComponent(it) }
            }
        }
        is BackgroundDecorator -> {
            // Zatím jen barevné pozadí, později sem dáme obrázek
            Box(modifier = modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
                component.slots?.content?.let { RenderComponent(it) }
            }
        }
        is OpacityDecorator -> {
            // Jen průhlednost, vykreslíme vnitřek
            component.slots?.content?.let { RenderComponent(it) }
        }
        is AudioDecorator -> {
            // Audio není vidět, jen vykreslíme vnitřek (logiku přehrávání budeme řešit jinde)
            component.slots?.content?.let { RenderComponent(it) }
        }

        // --- 3. OBSAH (Listy) ---
        is ImagerThing -> RenderImage(component, modifier)
        is TextThing -> RenderText(component, modifier)
        is VideoThing -> {
            // Placeholder pro video
            Box(modifier.background(Color.Black), contentAlignment = Alignment.Center) {
                Text("VIDEO: ${component.options?.uri ?: "No URI"}", color = Color.White)
            }
        }
        is EmptyThing -> {
            // Prázdné místo
            Spacer(modifier = modifier)
        }
    }
}

// --- POMOCNÉ FUNKCE PRO JEDNOTLIVÉ TYPY ---

@Composable
fun RenderGrid(component: GridLayout, modifier: Modifier) {
    val rows = component.options?.rows ?: 1
    val cols = component.options?.columns ?: 1
    val gap = component.options?.gap?.dp ?: 0.dp
    val slots = component.slots

    Column(modifier = modifier.fillMaxSize()) {
        // Jednoduchá implementace Gridu pomocí Row a Column
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
                            RenderComponent(item)
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
fun RenderSplit(component: SplitLayout, modifier: Modifier) {
    val isHorizontal = component.options?.horizontal == true
    val ratio = (component.options?.ratio ?: 50) / 100f // Převod 50 -> 0.5f
    val gap = component.options?.gap?.dp ?: 0.dp
    val first = component.slots?.first
    val second = component.slots?.second

    if (isHorizontal) {
        Row(modifier = modifier.fillMaxSize()) {
            Box(Modifier.weight(ratio).fillMaxHeight()) {
                if (first != null) RenderComponent(first)
            }
            Spacer(Modifier.width(gap))
            Box(Modifier.weight(1f - ratio).fillMaxHeight()) {
                if (second != null) RenderComponent(second)
            }
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            Box(Modifier.weight(ratio).fillMaxWidth()) {
                if (first != null) RenderComponent(first)
            }
            Spacer(Modifier.height(gap))
            Box(Modifier.weight(1f - ratio).fillMaxWidth()) {
                if (second != null) RenderComponent(second)
            }
        }
    }
}

@Composable
fun RenderImage(component: ImagerThing, modifier: Modifier) {
    val url = component.options?.url ?: ""

    // Rozhodneme, jak se má obrázek roztáhnout (cover = oříznout, contain = celá fotka)
    // Pokud není zadáno nic, použijeme Fit (celá fotka)
    val contentScale = when (component.options?.size) {
        "cover" -> ContentScale.Crop
        "contain" -> ContentScale.Fit
        else -> ContentScale.Fit
    }

    // Použijeme Coil pro načtení obrázku
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true) // Jemná animace načtení
            .build(),
        contentDescription = "Obrázek z alba",
        contentScale = contentScale,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun RenderText(component: TextThing, modifier: Modifier) {
    // ZATÍM JEDNODUCHÝ TEXT - Později musíme vyřešit HTML rendering
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Zjednodušeno, normálně bychom četli v-align
    ) {
        Text(
            // Odstraní HTML tagy pro náhled, aby text nebyl plný <h3> a <p>
            text = component.options?.html?.replace(Regex("<.*?>"), "") ?: "",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}