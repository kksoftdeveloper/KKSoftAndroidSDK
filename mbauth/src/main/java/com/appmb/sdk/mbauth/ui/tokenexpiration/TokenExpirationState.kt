package com.appmb.sdk.mbauth.ui.tokenexpiration

data class TokenExpirationState(
  val isLoading: Boolean = false,
  val error: Int? = null,
  val isLoggedOut: Boolean = false
)