package com.appmb.sdk.mbauth.ui.phoneinput

data class PhoneInputUiState(
  val phone: String = "",
  val acceptTerms: Boolean = false,
  val phoneError: String? = null,
  val isLoading: Boolean = false,
) {
  val canRequestOTP: Boolean
    get() = phone.isNotBlank() && phoneError == null && acceptTerms
}

