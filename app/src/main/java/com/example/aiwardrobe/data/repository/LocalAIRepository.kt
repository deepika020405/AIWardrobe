package com.example.aiwardrobe.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.palette.graphics.Palette
import com.example.aiwardrobe.model.ClothingAnalysis
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocalAIRepository(
    private val context: Context
) {

    suspend fun analyzeClothing(imageUri: Uri): Result<ClothingAnalysis> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("Could not open image file"))

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: return Result.failure(Exception("Could not decode image bitmap"))

            // Center-crop to focus on clothing item (ignore background edges)
            val croppedBitmap = centerCrop(originalBitmap)

            // 1. Dominant color extraction using center-cropped image and Palette API
            val colorName = detectDominantColor(croppedBitmap)

            // 2. Category detection using Google ML Kit On-Device Image Labeler & Aspect Ratio Heuristics
            val detectedCategory = detectCategoryWithMLKit(originalBitmap)

            // 3. Pattern & Style detection logic
            val pattern = detectPattern(croppedBitmap)
            val style = determineStyle(detectedCategory, colorName)
            val recommendation = generateRecommendation(detectedCategory, colorName, style)

            val analysis = ClothingAnalysis(
                category = detectedCategory,
                color = colorName,
                pattern = pattern,
                style = style,
                recommendation = recommendation
            )

            Log.d("AIWardrobe", "LOCAL ANALYSIS SUCCESS: $analysis")
            Result.success(analysis)
        } catch (e: Exception) {
            Log.e("AIWardrobe", "LOCAL ANALYSIS ERROR", e)
            Result.failure(e)
        }
    }

    private fun centerCrop(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val cropWidth = (width * 0.7).toInt()
        val cropHeight = (height * 0.7).toInt()
        val startX = (width - cropWidth) / 2
        val startY = (height - cropHeight) / 2

        return Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
    }

    private suspend fun detectCategoryWithMLKit(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val labelTexts = labels.map { it.text.lowercase() }
                    Log.d("AIWardrobe", "ML Kit Labels: $labelTexts")

                    var categoryFound: String? = null

                    for (label in labels) {
                        val text = label.text.lowercase()
                        when {
                            // Slippers / Footwear
                            text.contains("footwear") || text.contains("shoe") || text.contains("slipper") ||
                            text.contains("sandal") || text.contains("sneaker") || text.contains("boot") ||
                            text.contains("sock") || text.contains("clog") || text.contains("outdoor shoe") ||
                            text.contains("walking shoe") -> {
                                categoryFound = "Slippers / Footwear"
                                break
                            }
                            // Jeans / Lowers / Trousers / Pants
                            text.contains("pants") || text.contains("trousers") || text.contains("jeans") ||
                            text.contains("denim") || text.contains("leggings") || text.contains("sweatpants") ||
                            text.contains("slacks") || text.contains("chinos") || text.contains("bottom") -> {
                                categoryFound = "Jeans / Lowers"
                                break
                            }
                            // Shorts
                            text.contains("shorts") || text.contains("trunks") -> {
                                categoryFound = "Shorts"
                                break
                            }
                            // Jackets / Coats / Outerwear
                            text.contains("jacket") || text.contains("coat") || text.contains("blazer") ||
                            text.contains("outerwear") || text.contains("hoodie") || text.contains("vest") -> {
                                categoryFound = "Jacket"
                                break
                            }
                            // Dresses & Skirts
                            text.contains("dress") || text.contains("gown") -> {
                                categoryFound = "Dress"
                                break
                            }
                            text.contains("skirt") -> {
                                categoryFound = "Skirt"
                                break
                            }
                            // Sweaters
                            text.contains("sweater") || text.contains("cardigan") || text.contains("jumper") -> {
                                categoryFound = "Sweater"
                                break
                            }
                            // Tops & Shirts
                            text.contains("t-shirt") || text.contains("jersey") || text.contains("tee") -> {
                                categoryFound = "T-shirt"
                                break
                            }
                            text.contains("shirt") || text.contains("blouse") || text.contains("sleeve") -> {
                                categoryFound = "Shirt"
                                break
                            }
                        }
                    }

                    // Aspect ratio fallback if ML Kit labels are generic (e.g. "Textile", "Pattern")
                    if (categoryFound == null) {
                        val heightToWidth = bitmap.height.toFloat() / bitmap.width.toFloat()
                        categoryFound = when {
                            heightToWidth > 1.35f -> "Jeans / Lowers"
                            heightToWidth < 0.85f -> "Slippers / Footwear"
                            else -> "T-shirt"
                        }
                    }

                    continuation.resume(categoryFound)
                }
                .addOnFailureListener {
                    continuation.resume("Top")
                }
        }

    private fun detectDominantColor(bitmap: Bitmap): String {
        val palette = Palette.from(bitmap).generate()

        val selectedSwatch = palette.vibrantSwatch
            ?: palette.lightVibrantSwatch
            ?: palette.darkVibrantSwatch
            ?: palette.mutedSwatch
            ?: palette.dominantSwatch

        if (selectedSwatch != null) {
            val color = selectedSwatch.rgb
            val red = (color shr 16) and 0xFF
            val green = (color shr 8) and 0xFF
            val blue = color and 0xFF
            return getColorName(red, green, blue)
        }
        return "Blue"
    }

    private fun getColorName(r: Int, g: Int, b: Int): String {
        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)
        val hue = hsv[0]
        val sat = hsv[1]
        val valBrightness = hsv[2]

        if (valBrightness < 0.22f) return "Black"
        if (valBrightness > 0.85f && sat < 0.18f) return "White"
        if (sat < 0.18f) return "Grey"

        return when {
            hue < 15 || hue >= 345 -> "Red"
            hue < 45 -> if (valBrightness < 0.5f) "Brown" else "Orange"
            hue < 70 -> "Yellow"
            hue < 165 -> "Green"
            hue < 255 -> "Blue"
            hue < 315 -> "Purple"
            else -> "Pink"
        }
    }

    private fun detectPattern(bitmap: Bitmap): String {
        val width = bitmap.width
        val height = bitmap.height
        val stepX = width / 5
        val stepY = height / 5

        var minLum = 255f
        var maxLum = 0f

        for (x in stepX until width step stepX) {
            for (y in stepY until height step stepY) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                if (lum < minLum) minLum = lum
                if (lum > maxLum) maxLum = lum
            }
        }

        return if ((maxLum - minLum) > 90f) "Patterned / Graphic" else "Solid / Plain"
    }

    private fun determineStyle(category: String, color: String): String {
        return when {
            category.contains("Slippers", ignoreCase = true) || category.contains("Footwear", ignoreCase = true) -> "Casual / Relaxed Footwear"
            category.contains("Jeans", ignoreCase = true) || category.contains("Lowers", ignoreCase = true) -> "Casual Everyday Bottom"
            category.contains("Shirt", ignoreCase = true) || category.contains("Jacket", ignoreCase = true) -> "Smart Casual / Formal"
            category.contains("Dress", ignoreCase = true) -> "Elegant / Chic"
            else -> "Casual"
        }
    }

    private fun generateRecommendation(category: String, color: String, style: String): String {
        val darkOrNeutral = color in listOf("Black", "Navy", "Grey", "White", "Brown")
        val bottomPairing = if (darkOrNeutral) "light-colored chinos or blue jeans" else "dark trousers or black jeans"

        return when {
            category.contains("Slippers", ignoreCase = true) || category.contains("Footwear", ignoreCase = true) ->
                "These $color $category pair great with casual shorts, sweatpants, or loungewear for comfortable daily wear."
            category.contains("Jeans", ignoreCase = true) || category.contains("Lowers", ignoreCase = true) ->
                "Match these $color $category with a clean white t-shirt or casual denim jacket for a classic, relaxed look."
            category.contains("T-shirt", ignoreCase = true) ->
                "This $color $category pairs great with $bottomPairing and white sneakers for an effortless everyday look."
            category.contains("Shirt", ignoreCase = true) ->
                "Combine this $color $category with tailored $bottomPairing and leather shoes for a sharp, confident style."
            category.contains("Jacket", ignoreCase = true) ->
                "Layer this $color $category over a plain white or black tee to instantly elevate your look."
            category.contains("Dress", ignoreCase = true) ->
                "Style this $color $category with minimal gold/silver accessories and comfortable heels or flats."
            category.contains("Skirt", ignoreCase = true) ->
                "Match this $color $category with a fitted top and stylish footwear."
            category.contains("Sweater", ignoreCase = true) ->
                "Layer this $color $category over a button-down shirt for cozy, smart-casual warmth."
            else ->
                "A great $color $category! Combine with neutral wardrobe essentials for a well-balanced look."
        }
    }
}
