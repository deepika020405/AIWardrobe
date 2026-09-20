package com.example.aiwardrobe

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiwardrobe.ui.screens.AddClothingScreen
import com.example.aiwardrobe.ui.screens.AnalysisScreen
import com.example.aiwardrobe.ui.screens.OutfitRecommendationScreen
import com.example.aiwardrobe.ui.screens.WardrobeScreen
import com.example.aiwardrobe.ui.theme.AIWardrobeTheme
import com.example.aiwardrobe.ui.viewmodel.WardrobeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WardrobeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AIWardrobeTheme {
                val wardrobeItems by viewModel.wardrobeItems.collectAsStateWithLifecycle()
                val analysis by viewModel.analysis.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val error by viewModel.error.collectAsStateWithLifecycle()
                val currentUri by viewModel.currentImageUri.collectAsStateWithLifecycle()

                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.Checkroom, contentDescription = "Wardrobe") },
                                label = { Text("Wardrobe") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Scan") },
                                label = { Text("Scan") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.Style, contentDescription = "Outfits") },
                                label = { Text("Outfits") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> WardrobeScreen(
                                items = wardrobeItems,
                                onDeleteItem = { item -> viewModel.deleteClothingItem(item) },
                                onAddNewItem = { selectedTab = 1 }
                            )
                            1 -> {
                                if (isLoading || analysis != null || error != null) {
                                    AnalysisScreen(
                                        analysis = analysis,
                                        isLoading = isLoading,
                                        error = error,
                                        onSave = {
                                            if (analysis != null && currentUri != null) {
                                                viewModel.saveClothingItem(analysis!!, currentUri!!)
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Saved to Digital Wardrobe!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                selectedTab = 0
                                            }
                                        },
                                        onBack = {
                                            viewModel.resetState()
                                        }
                                    )
                                } else {
                                    AddClothingScreen(
                                        onAnalyze = { uri ->
                                            viewModel.analyzeClothing(uri)
                                        }
                                    )
                                }
                            }
                            2 -> OutfitRecommendationScreen(
                                items = wardrobeItems,
                                onScanMore = { selectedTab = 1 }
                            )
                        }
                    }
                }
            }
        }
    }
}