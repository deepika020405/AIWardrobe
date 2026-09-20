package com.example.aiwardrobe.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiwardrobe.BuildConfig
import com.example.aiwardrobe.data.local.ClothingEntity
import com.example.aiwardrobe.data.local.WardrobeDatabase
import com.example.aiwardrobe.data.repository.OpenAIRepository
import com.example.aiwardrobe.model.ClothingAnalysis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WardrobeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        OpenAIRepository(
            context = application.applicationContext,
            apiKey = BuildConfig.OPENAI_API_KEY
        )

    private val clothingDao = WardrobeDatabase.getDatabase(application).clothingDao()

    val wardrobeItems: StateFlow<List<ClothingEntity>> = clothingDao.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentImageUri = MutableStateFlow<Uri?>(null)
    val currentImageUri: StateFlow<Uri?> = _currentImageUri.asStateFlow()

    private val _analysis = MutableStateFlow<ClothingAnalysis?>(null)
    val analysis: StateFlow<ClothingAnalysis?> = _analysis.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun analyzeClothing(imageUri: Uri) {
        _currentImageUri.value = imageUri
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _analysis.value = null

            val result = repository.analyzeClothing(imageUri)

            result
                .onSuccess { clothingAnalysis ->
                    _analysis.value = clothingAnalysis
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Something went wrong"
                }

            _isLoading.value = false
        }
    }

    fun saveClothingItem(analysis: ClothingAnalysis, imageUri: Uri) {
        viewModelScope.launch {
            val entity = ClothingEntity(
                imageUri = imageUri.toString(),
                category = analysis.category,
                color = analysis.color,
                pattern = analysis.pattern,
                style = analysis.style,
                recommendation = analysis.recommendation
            )
            clothingDao.insertItem(entity)
            resetState()
        }
    }

    fun deleteClothingItem(item: ClothingEntity) {
        viewModelScope.launch {
            clothingDao.deleteItem(item)
        }
    }

    fun resetState() {
        _analysis.value = null
        _error.value = null
        _isLoading.value = false
        _currentImageUri.value = null
    }
}