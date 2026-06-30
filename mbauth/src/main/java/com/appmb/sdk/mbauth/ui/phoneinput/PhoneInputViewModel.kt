package com.appmb.sdk.mbauth.ui.phoneinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RequestOtpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneInputViewModel : ViewModel() {

  val requestOtpState = MutableStateFlow<RequestOtpState>(RequestOtpState.Idle)

  private val _uiState = MutableStateFlow(PhoneInputUiState())
  val uiState: StateFlow<PhoneInputUiState> = _uiState.asStateFlow()

  private val phoneRegex = Regex("""^(?:\+?84|0)([35789])\d{8}$""")

  fun onAcceptTermsChange(accepted: Boolean) {
    _uiState.update { it.copy(acceptTerms = accepted) }
  }

  fun onConfirmedAge16OrOlderChange(confirmed: Boolean) {
    _uiState.update { it.copy(confirmedAge16OrOlder = confirmed) }
  }

  fun onPhoneChange(phone: String) {
    val error = when {
      phone.isBlank() -> "Thông tin đăng nhập không hợp lệ"
      !phoneRegex.matches(phone) -> "Thông tin đăng nhập không hợp lệ"
      else -> null
    }
    requestOtpState.value = RequestOtpState.Idle
    _uiState.update { it.copy(phone = phone, phoneError = error) }
  }

  fun dispatchUiEvent(intent: PhoneInputIntent) {
    when (intent) {
      is PhoneInputIntent.RequestOtp -> {
        sendRequestOtp(intent)
      }

      is PhoneInputIntent.ResetState -> {
        requestOtpState.update { RequestOtpState.Idle }
      }
    }
  }

  private fun sendRequestOtp(intent: PhoneInputIntent.RequestOtp) {
    viewModelScope.launch {
      // Show loading
      requestOtpState.update { RequestOtpState.Loading }
      // Call verify otp api
      MbAuth.requestOtp(
        MbAuthParams.buildRequestOtp(
          phone = intent.phone,
          type = intent.otpType,
        )
      ) { result ->
        when (result) {
          is RequestOtpResult.Error -> {
            requestOtpState.update {
              RequestOtpState.Error(
                code = result.code,
                message = result.message.orEmpty()
              )
            }
          }

          is RequestOtpResult.Success -> {
            requestOtpState.update {
              RequestOtpState.Success(
                phone = intent.phone,
                timeToRetry = result.data.retryAfterSeconds ?: 0,
              )
            }
          }
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    _uiState.value = PhoneInputUiState()
  }
}
