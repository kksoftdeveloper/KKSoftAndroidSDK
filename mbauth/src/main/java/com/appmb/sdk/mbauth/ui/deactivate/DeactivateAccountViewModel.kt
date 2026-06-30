package com.appmb.sdk.mbauth.ui.deactivate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.core.auth.MbAuthManager
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbcore.model.DeactivateAccountResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeactivateAccountViewModel internal constructor(
  private val mbAuthManager: MbAuthManager,
) : ViewModel() {
  private val _deactivateAccountResult = MutableStateFlow<AuthResult?>(null)
  val deactivateAccountResult: StateFlow<AuthResult?> = _deactivateAccountResult.asStateFlow()

  fun deactivate() {
    viewModelScope.launch {
      mbAuthManager.deactivateAccount().collect { result ->
        when (result) {
          is DeactivateAccountResult.Error -> {
            _deactivateAccountResult.update {
              AuthResult.Failure(status = result.code, msg = result.message)
            }
          }

          DeactivateAccountResult.Success -> {
            _deactivateAccountResult.update {
              AuthResult.DeactivateAccount(true)
            }
          }
        }
      }
    }
  }
}