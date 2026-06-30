package com.appmb.sdk.mbauth.ui.passwordinput

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PasswordInputViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(PasswordInputUiState())
  val uiState: StateFlow<PasswordInputUiState> = _uiState.asStateFlow()

  fun onPasswordChange(password: String) {
    _uiState.update { it.copy(password = password) }
  }

  fun onConfirmPasswordChange(confirmPassword: String) {
    _uiState.update { it.copy(confirmPassword = confirmPassword) }
  }
}