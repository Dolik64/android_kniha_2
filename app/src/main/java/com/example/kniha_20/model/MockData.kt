package com.example.kniha_20.model

object MockData {

    // Helper funkce pro cestu k assetům
    private fun asset(fileName: String): String = "file:///android_asset/$fileName"

    // Toto je naše "natvrdo" definované album
    fun getTestAlbum(): BookComponent {
        return BackgroundDecorator(
            slots = ContentSlot(
                content = InsetDecorator(
                    options = InsetOptions(top = 20.0, left = 20.0, right = 20.0, bottom = 20.0),
                    slots = ContentSlot(
                        content = GridLayout(
                            options = GridOptions(rows = 2, columns = 1, gap = 15),
                            slots = listOf(
                                // 1. OBRÁZEK Z TVÉ SLOŽKY ASSETS
                                ImagerThing(
                                    options = ImageOptions(
                                        url = asset("foto1.jpg"), // Změň na název své fotky
                                        size = "cover"
                                    )
                                ),
                                // 2. OBRÁZEK Z TVÉ SLOŽKY ASSETS
                                ImagerThing(
                                    options = ImageOptions(
                                        url = asset("foto2.jpg"), // Změň na název své fotky
                                        size = "contain"
                                    )
                                ),
                                // Text pod tím
                                TextThing(
                                    options = TextOptions(
                                        html = "<h3>Moje testovací galerie</h3><p>Tyto obrázky se načítají lokálně ze složky assets.</p>",
                                        fontSize = "16px"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}