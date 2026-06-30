package com.appmb.sdk.mbauth.ui.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbcore.model.server.GetListServerIdsResult
import com.appmb.sdk.mbauth.model.UpdateServerIdResult
import com.appmb.sdk.mbauth.ui.login.GetGameUuidState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServerViewModel internal constructor() : ViewModel() {

  private val _uiState = MutableStateFlow(ServerListState())
  val uiState: StateFlow<ServerListState> = _uiState.asStateFlow()

  val serverState = MutableStateFlow<ServerOperationState>(ServerOperationState.Idle)
  val getGameUuidState = MutableStateFlow<GetGameUuidState>(GetGameUuidState.Idle)

  init {
    handleGetListServerIds()
  }

  fun onAction(action: ServerListAction) {
    when (action) {
      is ServerListAction.GetListServerIds -> handleGetListServerIds()
      is ServerListAction.GetGameUuid -> {
        handleGetGameUuid(action)
      }
      is ServerListAction.UpdateServerClientId -> {
        handleUpdateServerClientId(action)
      }
    }
  }

  private fun handleGetListServerIds() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      MbAuth.getListServerIds { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is GetListServerIdsResult.Error -> {
            serverState.update {
              ServerOperationState.Error(result.message ?: "Unknown Error!")
            }
          }

          is GetListServerIdsResult.Success -> {
            serverState.update {
              if (result.data.isNotEmpty()) {
                ServerOperationState.GetServerIdsSuccess(result.data)
              } else {
                ServerOperationState.Maintenance
              }
            }
          }
        }
      }
    }
  }

  private fun handleGetGameUuid(intent: ServerListAction.GetGameUuid) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      val serverId = intent.serverId
      MbAuth.getGameUuid(serverId) { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is UpdateServerIdResult.Error -> {
            getGameUuidState.update {
              GetGameUuidState.Error(result.message ?: "Unknown Error!")
            }
          }

          is UpdateServerIdResult.Success -> {
            getGameUuidState.update {
              GetGameUuidState.Success(result.authData, result.characterId)
            }
          }
        }
      }
    }
  }

  /**
   * Example implementation of updateServerClientId
   * 
   * This method demonstrates how to use the updateServerClientId API.
   * It handles both authenticated and unauthenticated scenarios.
   */
  private fun handleUpdateServerClientId(intent: ServerListAction.UpdateServerClientId) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      val serverId = intent.serverId
      
      MbAuth.updateServerClientId(serverId) { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is UpdateServerIdResult.Error -> {
            // Handle different error codes
            when (result.code) {
              401 -> {
                // User is not authenticated - serverId saved locally only
                serverState.update {
                  ServerOperationState.UpdateServerClientIdUnauthenticated(
                    result.message ?: "Not authenticated. Server ID saved locally."
                  )
                }
              }
              400 -> {
                // Invalid serverId (null or empty when authenticated)
                serverState.update {
                  ServerOperationState.Error(
                    result.message ?: "Invalid server ID"
                  )
                }
              }
              else -> {
                // Network or API error
                serverState.update {
                  ServerOperationState.Error(
                    result.message ?: "Failed to update server ID"
                  )
                }
              }
            }
          }

          is UpdateServerIdResult.Success -> {
            // Success - gameUuid and characterId are saved
            serverState.update {
              ServerOperationState.UpdateServerClientIdSuccess(result.authData, result.characterId)
            }
            
            // Also update the getGameUuidState for compatibility
            getGameUuidState.update {
              GetGameUuidState.Success(result.authData, result.characterId)
            }
          }
        }
      }
    }
  }
}