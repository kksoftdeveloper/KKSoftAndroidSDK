package com.appmb.sdk.mbauth.ui.login

data class UiState(
  val phone: String = "",
  val password: String = "",
  val gameId: Int? = null,
  val acceptTerms: Boolean = false,
  val isLoading: Boolean = false,
  val errorCode: Int? = null,
  val isGoogleEnabled: Boolean = false,
  val isFacebookEnabled: Boolean = false
) {

  val canLogin: Boolean
    get() = phone.isNotBlank() && password.isNotBlank() && acceptTerms
}

