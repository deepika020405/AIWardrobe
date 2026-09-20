package com.example.aiwardrobe.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.aiwardrobe.data.local.ClothingEntity

data class OutfitCombination(
    val title: String,
    val top: ClothingEntity?,
    val bottom: ClothingEntity?,
    val footwear: ClothingEntity?,
    val description: String
)

@Composable
fun OutfitRecommendationScreen(
    items: List<ClothingEntity>,
    onScanMore: () -> Unit
) {
    val tops = items.filter { item ->
        val cat = item.category.lowercase()
        cat.contains("shirt") || cat.contains("top") || cat.contains("jacket") || cat.contains("sweater") || cat.contains("upper") || cat.contains("hoodie")
    }
    val bottoms = items.filter { item ->
        val cat = item.category.lowercase()
        cat.contains("jean") || cat.contains("trouser") || cat.contains("short") || cat.contains("skirt") || cat.contains("lower") || cat.contains("pant")
    }
    val footwearList = items.filter { item ->
        val cat = item.category.lowercase()
        cat.contains("footwear") || cat.contains("slipper") || cat.contains("shoe") || cat.contains("sneaker") || cat.contains("sandal")
    }

    val outfitCombinations = mutableListOf<OutfitCombination>()

    if (tops.isNotEmpty() && bottoms.isNotEmpty()) {
        val count = minOf(3, maxOf(tops.size, bottoms.size))
        for (i in 0 until count) {
            val top = if (tops.isNotEmpty()) tops[i % tops.size] else null
            val bottom = if (bottoms.isNotEmpty()) bottoms[i % bottoms.size] else null
            val shoes = if (footwearList.isNotEmpty()) footwearList[i % footwearList.size] else null

            val outfitTitle = when {
                top?.category?.lowercase()?.contains("shirt") == true -> "Smart Casual Outfit #${i + 1}"
                top?.category?.lowercase()?.contains("jacket") == true -> "Layered Style Outfit #${i + 1}"
                else -> "Everyday Casual Outfit #${i + 1}"
            }

            outfitCombinations.add(
                OutfitCombination(
                    title = outfitTitle,
                    top = top,
                    bottom = bottom,
                    footwear = shoes,
                    description = "Pair this ${top?.color ?: ""} ${top?.category ?: "Top"} with your ${bottom?.color ?: ""} ${bottom?.category ?: "Bottom"} for a perfectly balanced look."
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Outfits",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "AI Outfit Recommendations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Smart outfit pairings created from your wardrobe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (outfitCombinations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Add at least 1 Top and 1 Bottom to generate AI outfits!",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onScanMore) {
                        Text("Scan Clothing Items")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(outfitCombinations) { combination ->
                    OutfitCard(combination = combination)
                }
            }
        }
    }
}

@Composable
private fun OutfitCard(combination: OutfitCombination) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = combination.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = combination.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                combination.top?.let { item ->
                    ClothingPieceThumbnail(item = item, label = "Top")
                }
                combination.bottom?.let { item ->
                    ClothingPieceThumbnail(item = item, label = "Bottom")
                }
                combination.footwear?.let { item ->
                    ClothingPieceThumbnail(item = item, label = "Footwear")
                }
            }
        }
    }
}

@Composable
private fun RowScope.ClothingPieceThumbnail(item: ClothingEntity, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(item.imageUri)),
                contentDescription = item.category,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(text = item.category, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
