package com.example.aiwardrobe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true, showSystemUi = true)
@Composable


fun HomeScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "My Wardrobe",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Your digital closet",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = "Wardrobe"
                )

                Spacer(modifier = Modifier.padding(8.dp))

                Column {
                    Text(
                        text = "0 Items",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Start building your wardrobe"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // We'll open camera here
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add"
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Text("Add Clothing")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Outfit recommendation later
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.Style,
                contentDescription = "AI Outfit"
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Text("AI Outfit Recommendation")
        }
    }
}