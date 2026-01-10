@file:OptIn(ExperimentalPageCurlApi::class)

package com.example.kniha_20

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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

@Composable
fun KnihaApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToPlayer = { navController.navigate(PlayerRoute) },
                onNavigateToEditor = { navController.navigate(EditorRoute) }
            )
        }

        composable<PlayerRoute> {
            BookSpreadByClick()
        }

        composable<EditorRoute> {
            EditorScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Obrazovka editoru s tlačítkem pro nahrání VŠECH fotek z assets
 */
@Composable
fun EditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Stav dat stránky
    var pageData by remember {
        mutableStateOf<BookComponent>(
            BackgroundDecorator(
                slots = ContentSlot(
                    content = InsetDecorator(
                        options = InsetOptions(top = 40.0, left = 20.0, right = 20.0, bottom = 40.0),
                        slots = ContentSlot(
                            content = GridLayout(
                                options = GridOptions(rows = 2, columns = 1, gap = 10),
                                slots = listOf(
                                    ImagerThing(options = ImageOptions(url = "file:///android_asset/foto1.jpg")),
                                    TextThing(options = TextOptions(html = "Klikni na obrázek pro změnu"))
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    var activeImageId by remember { mutableStateOf<String?>(null) }

    // Launcher pro výběr fotek
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null && activeImageId != null) {
                // Udělíme trvalá práva ke čtení URI
                try {
                    val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: Exception) {
                    // Ignorujeme
                }

                pageData = updateImageInTree(pageData, activeImageId!!, uri.toString())
                activeImageId = null
            }
        }
    )

    Box(Modifier.fillMaxSize().background(Color.White)) {
        RenderComponent(
            component = pageData,
            onImageClick = { clickedId ->
                activeImageId = clickedId
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        // OVLÁDACÍ TLAČÍTKA DOLE
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    scope.launch {
                        copyAllAssetsToGallery(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Nahrát všechny fotky z Assets")
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = onBack) {
                Text("Zpět")
            }
        }
    }
}

/**
 * ZVÝRAZNĚNÁ ZMĚNA: Funkce, která dynamicky projde složku assets a zkopíruje vše
 */
suspend fun copyAllAssetsToGallery(context: Context) {
    withContext(Dispatchers.IO) {
        val assetManager = context.assets
        var copiedCount = 0

        try {
            // 1. Získáme seznam všech souborů v kořenu assets ("")
            val allFiles = assetManager.list("") ?: emptyArray()

            // 2. Vyfiltrujeme jen obrázky (podle koncovky)
            val imageFiles = allFiles.filter { fileName ->
                val lower = fileName.lowercase()
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
            }

            imageFiles.forEach { fileName ->
                try {
                    // Určíme MIME type podle koncovky
                    val mimeType = if (fileName.lowercase().endsWith(".png")) "image/png" else "image/jpeg"

                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "Demo_$fileName")
                        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KnihaApp")
                        }
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                    uri?.let { outputUri ->
                        assetManager.open(fileName).use { inputStream ->
                            resolver.openOutputStream(outputUri).use { outputStream ->
                                inputStream.copyTo(outputStream!!)
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(outputUri, contentValues, null, null)
                        }
                        copiedCount++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Nahráno $copiedCount fotek do Galerie", Toast.LENGTH_SHORT).show()
        }
    }
}

fun updateImageInTree(component: BookComponent, targetId: String, newUrl: String): BookComponent {
    return when (component) {
        is ImagerThing -> {
            if (component.id == targetId) {
                component.copy(options = component.options?.copy(url = newUrl) ?: ImageOptions(url = newUrl))
            } else {
                component
            }
        }
        is BackgroundDecorator -> component.copy(
            slots = component.slots?.let { it.copy(content = updateImageInTree(it.content, targetId, newUrl)) }
        )
        is InsetDecorator -> component.copy(
            slots = component.slots?.let { it.copy(content = updateImageInTree(it.content, targetId, newUrl)) }
        )
        is GridLayout -> component.copy(
            slots = component.slots.map { updateImageInTree(it, targetId, newUrl) }
        )
        else -> component
    }
}

// ... KNIHA PŘEHRÁVAČ ...

@Composable
fun BookSpreadByClick() {
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

@Composable
fun NavImageButton(resId: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier.size(130.dp)) {
        Image(painter = painterResource(id = resId), contentDescription = "Navigace", modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun PageFace(label: String, hint: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (label.isNotEmpty()) Text(label, fontSize = 22.sp)
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
    Kniha_20Theme { KnihaApp() }
}