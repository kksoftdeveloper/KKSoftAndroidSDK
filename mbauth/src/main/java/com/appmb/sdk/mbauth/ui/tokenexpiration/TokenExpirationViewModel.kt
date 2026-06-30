package com.appmb.sdk.mbauth.ui.tokenexpiration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.core.auth.MbAuthManager
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.model.LogoutResult
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TokenExpirationViewModel internal constructor(
  private val mbAuthManager: MbAuthManager,
) : ViewModel() {

  var state by mutableStateOf(TokenExpirationState())
    private set

  private val eventChanel = Channel<TokenExpirationEvent>()
  val events = eventChanel.receiveAsFlow()

  fun onAction(action: TokenExpirationAction) {
    when (action) {
      is TokenExpirationAction.OnClickConfirmLogin -> {
        state = state.copy(isLoading = true, error = null, isLoggedOut = false)
        logout()
      }
    }
  }

//  private val _logoutResult = MutableStateFlow<AuthResult?>(null)
//  val logoutResult: StateFlow<AuthResult?> = _logoutResult.asStateFlow()


  fun logout() {
    viewModelScope.launch {
      mbAuthManager
        .logout()
        .collect { result ->
          eventChanel.send(TokenExpirationEvent.OpenLoginScreen)
        }

//      mbAuthManager.logout().collect { result ->
//        when (result) {
//          is LogoutResult.Success -> {
//            _logoutResult.update { AuthResult.Logout(true) }
//          }
//
//          is LogoutResult.Error -> {
//            _logoutResult.update {
//              AuthResult.Failure(
//                status = result.status ?: AuthErrorCodeResponse.UnknownError.code,
//                msg = result.message ?: AuthErrorCodeResponse.UnknownError.description
//              )
//            }
//          }
//        }
//      }
    }
  }
}