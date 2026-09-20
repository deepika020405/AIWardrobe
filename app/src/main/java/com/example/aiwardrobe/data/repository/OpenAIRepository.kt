package com.example.aiwardrobe.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.aiwardrobe.data.remote.RetrofitClient
import com.example.aiwardrobe.model.ClothingAnalysis
import com.example.aiwardrobe.model.OllamaMessage
import com.example.aiwardrobe.model.OllamaRequest
import com.example.aiwardrobe.utils.imageUriToBase64
import com.google.gson.Gson
import java.io.InputStream

class OpenAIRepository(
    private val context: Context,
    private val apiKey: String
) {

    suspend fun analyzeClothing(
        imageUri: Uri
    ): Result<ClothingAnalysis> {

        Log.d("AIWardrobe", "API key length = ${apiKey.length}")

        return try {

            val inputStream: InputStream =
                context.contentResolver
                    .openInputStream(imageUri)
                    ?: throw Exception("Cannot open image")

            val bytes = inputStream.use {
                it.readBytes()
            }

            val base64Image = imageUriToBase64(context, imageUri)

            val prompt = """
                Analyze this clothing or footwear image carefully.

                Identify:
                1. Clothing category
                2. Main color
                3. Pattern
                4. Style
                5. Outfit recommendation

                Category must be one of:
                T-shirt, Shirt, Top, Jeans, Trousers, Shorts, Dress, Skirt, Jacket, Sweater, Footwear / Slippers, Other

                Return ONLY valid JSON with no markdown block formatting.

                Format:
                {
                  "category": "...",
                  "color": "...",
                  "pattern": "...",
                  "style": "...",
                  "recommendation": "..."
                }
            """.trimIndent()

            val request = OllamaRequest(
                model = "gemma4:31b",
                messages = listOf(
                    OllamaMessage(
                        role = "user",
                        content = prompt,
                        images = listOf(base64Image)
                    )
                ),
                stream = false
            )

            val response = RetrofitClient.api.analyzeImage(
                authorization = "Bearer $apiKey",
                request = request
            )

            val text = response.message?.content
                ?: throw Exception("Ollama AI returned no response")

            val jsonStart = text.indexOf('{')
            val jsonEnd = text.lastIndexOf('}')

            val cleanedJson = if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                text.substring(jsonStart, jsonEnd + 1)
            } else {
                text.replace("```json", "").replace("```", "").trim()
            }

            Log.d("AIWardrobe", "CLEANED JSON = $cleanedJson")

            val analysis = Gson().fromJson(cleanedJson, ClothingAnalysis::class.java)

            Result.success(analysis)

        } catch (e: retrofit2.HttpException) {
            val errorDetails = e.response()?.errorBody()?.string() ?: e.message()
            Log.e("AIWardrobe", "Ollama HTTP Error ${e.code()}: $errorDetails")
            if (e.code() == 400 || e.code() == 401 || e.code() == 403) {
                Result.failure(Exception("Ollama API Key Invalid or Unauthorized (${e.code()}). Check key in local.properties."))
            } else {
                Result.failure(Exception("Ollama API Error (${e.code()}): $errorDetails"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
