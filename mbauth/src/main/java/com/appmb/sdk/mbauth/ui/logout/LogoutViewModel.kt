package com.appmb.sdk.mbauth.ui.logout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.core.auth.MbAuthManager
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.model.LogoutResult
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogoutViewModel internal constructor(
  private val mbAuthManager: MbAuthManager,
) : ViewModel() {
  private val _logoutResult = MutableStateFlow<AuthResult?>(null)
  val logoutResult: StateFlow<AuthResult?> = _logoutResult.asStateFlow()


  fun logout() {
    viewModelScope.launch {
      mbAuthManager.logout().collect { result ->
        when (result) {
          is LogoutResult.Success -> {
            _logoutResult.update { AuthResult.Logout(true) }
          }

          is LogoutResult.Error -> {
            _logoutResult.update {
              AuthResult.Failure(
                status = result.status ?: AuthErrorCodeResponse.UnknownError.code,
                msg = result.message ?: AuthErrorCodeResponse.UnknownError.description
              )
            }
          }
        }
      }
    }
  }
}