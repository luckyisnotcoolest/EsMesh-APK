package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.EsMeshMessage
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<EsMeshMessage> = emptyList(),
    val rawMessages: List<EsMeshMessage> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val inputText: String = "",
    val destinationNode: String = "BROADCAST",
    val isConnected: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val messageRepository: MessageRepository,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            messageRepository.allMessages.collect { allMsgs ->
                _uiState.value = _uiState.value.copy(
                    rawMessages = allMsgs,
                    messages = filterMessages(allMsgs, _uiState.value.searchQuery)
                )
            }
        }

        viewModelScope.launch {
            connectionRepository.connectedDevice.collect { dev ->
                _uiState.value = _uiState.value.copy(isConnected = dev != null)
            }
        }
    }

    private fun filterMessages(messages: List<EsMeshMessage>, query: String): List<EsMeshMessage> {
        if (query.isBlank()) return messages
        return messages.filter {
            it.payload.contains(query, ignoreCase = true) ||
            it.source.contains(query, ignoreCase = true) ||
            it.id.contains(query, ignoreCase = true)
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotBlank(),
            messages = filterMessages(_uiState.value.rawMessages, query)
        )
    }

    fun onDestinationChanged(node: String) {
        _uiState.value = _uiState.value.copy(destinationNode = node)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val dest = _uiState.value.destinationNode
        _uiState.value = _uiState.value.copy(
            inputText = "",
            isSending = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                connectionRepository.sendMessage(text, dest)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to send")
            } finally {
                _uiState.value = _uiState.value.copy(isSending = false)
            }
        }
    }

    fun retryMessage(msg: EsMeshMessage) {
        viewModelScope.launch {
            connectionRepository.sendMessage(msg.payload, msg.destination)
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            messageRepository.clearAll()
        }
    }

    class Factory(
        private val messageRepo: MessageRepository,
        private val connectionRepo: ConnectionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(messageRepo, connectionRepo) as T
        }
    }
}
