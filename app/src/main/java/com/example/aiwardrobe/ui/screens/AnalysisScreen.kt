//package com.example.aiwardrobe.ui.screens

package com.example.aiwardrobe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aiwardrobe.model.ClothingAnalysis

@Composable
fun AnalysisScreen(
    analysis: ClothingAnalysis?,
    isLoading: Boolean,
    error: String?,
    onSave: () -> Unit,
    onBack: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "AI Analysis",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // Loading state
        if (isLoading) {

            CircularProgressIndicator()

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "AI is analyzing your clothing..."
            )

        }

        // Error state
        else if (error != null) {

            Text(
                text = "AI Analysis Failed",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Try Again / Go Back")
            }

        }

        // Success state
        else if (analysis != null) {

            ClothingInfoCard(
                analysis = analysis
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save to Wardrobe")
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan Another Item")
            }
        }
    }
}

@Composable
private fun ClothingInfoCard(
    analysis: ClothingAnalysis
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Category: ${analysis.category}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Color: ${analysis.color}"
            )

            Text(
                text = "Pattern: ${analysis.pattern}"
            )

            Text(
                text = "Style: ${analysis.style}"
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "AI Recommendation",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = analysis.recommendation
            )
        }
    }
}