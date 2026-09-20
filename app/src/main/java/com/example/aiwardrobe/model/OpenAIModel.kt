package com.example.aiwardrobe.model

data class OllamaRequest(
    val model: String = "gemma4:31b",
    val messages: List<OllamaMessage>,
    val stream: Boolean = false
)

data class OllamaMessage(
    val role: String = "user",
    val content: String,
    val images: List<String>? = null
)

data class OllamaResponse(
    val model: String?,
    val message: OllamaResponseMessage?,
    val done: Boolean?
)

data class OllamaResponseMessage(
    val role: String?,
    val content: String?
)
