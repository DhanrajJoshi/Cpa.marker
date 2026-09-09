package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactMessage
import com.example.data.model.ImageItem
import com.example.data.model.RewardOffer
import com.example.data.repository.ImageRepository
import com.example.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val imageRepository = ImageRepository(application)
    val portfolioRepository = PortfolioRepository(application)

    val gallery: StateFlow<List<ImageItem>> = imageRepository.images
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewards: StateFlow<List<RewardOffer>> = portfolioRepository.rewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactMessages: StateFlow<List<ContactMessage>> = portfolioRepository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prompt = MutableStateFlow("")
    val selectedSize = MutableStateFlow("512x512")
    val selectedQuality = MutableStateFlow("standard")

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _previewImage = MutableStateFlow<ImageItem?>(null)
    val previewImage: StateFlow<ImageItem?> = _previewImage.asStateFlow()

    init {
        // Initialize preview with the latest image in gallery if available
        viewModelScope.launch {
            gallery.collect { list ->
                if (_previewImage.value == null && list.isNotEmpty()) {
                    _previewImage.value = list.first()
                }
            }
        }
    }

    fun setPromptText(text: String) {
        prompt.value = text
        _errorMessage.value = null
    }

    fun setSize(size: String) {
        selectedSize.value = size
    }

    fun setQuality(quality: String) {
        selectedQuality.value = quality
    }

    fun setPreview(item: ImageItem) {
        _previewImage.value = item
    }

    fun applyPromptSuggestion(suggestedPrompt: String) {
        prompt.value = suggestedPrompt
        _errorMessage.value = null
    }

    fun generate() {
        val currentPrompt = prompt.value.trim()
        if (currentPrompt.isEmpty()) {
            _errorMessage.value = "Please enter a prompt."
            return
        }

        _isGenerating.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = imageRepository.generateImage(
                prompt = currentPrompt,
                size = selectedSize.value,
                quality = selectedQuality.value
            )

            result.onSuccess { item ->
                _previewImage.value = item
                _isGenerating.value = false
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Generation failed. Please try again."
                _isGenerating.value = false
            }
        }
    }

    fun deleteImage(id: String) {
        if (_previewImage.value?.id == id) {
            _previewImage.value = null
        }
        imageRepository.deleteImage(id)
    }

    fun unlockReward(id: String) {
        portfolioRepository.unlockReward(id)
    }

    fun sendContactMessage(name: String, email: String, message: String): Result<Unit> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Please enter your name."))
        if (email.isBlank() || !email.contains("@")) return Result.failure(IllegalArgumentException("Please enter a valid email."))
        if (message.isBlank()) return Result.failure(IllegalArgumentException("Please enter your message."))

        portfolioRepository.submitContactMessage(name, email, message)
        return Result.success(Unit)
    }
}
