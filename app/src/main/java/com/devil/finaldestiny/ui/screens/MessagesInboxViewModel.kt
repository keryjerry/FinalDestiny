package com.devil.finaldestiny.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.model.DirectMessageConversation
import com.devil.finaldestiny.model.ProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesInboxViewModel : ViewModel() {
    private val _conversations = MutableStateFlow<List<DirectMessageConversation>>(emptyList())
    val conversations: StateFlow<List<DirectMessageConversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadConversations(currentUserId: String) {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = SupabaseAuthClient.fetchConversationsFromSupabase(currentUserId)
                _conversations.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Resilient Profile Name Resolution helper.
     * Checks username -> fullName -> displayName -> email -> phone -> sender metadata fallback.
     */
    fun resolveProfileName(profile: ProfileDto?, fallbackId: String?, senderMetadataName: String? = null): String {
        val resolved = profile?.getResolvedName()
        if (!resolved.isNullOrBlank() && !resolved.startsWith("User_")) {
            return resolved
        }
        val sName = senderMetadataName?.trim()?.takeIf { it.isNotBlank() && it.lowercase() != "null" }
        if (!sName.isNullOrBlank()) {
            return sName
        }
        return resolved ?: (fallbackId?.takeIf { it.isNotBlank() }?.let { "User_${it.take(5)}" } ?: "Destiny Creator")
    }
}
