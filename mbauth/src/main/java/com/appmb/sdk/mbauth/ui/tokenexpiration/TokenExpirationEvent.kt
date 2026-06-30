package com.appmb.sdk.mbauth.ui.tokenexpiration

sealed interface TokenExpirationEvent {
  object OpenLoginScreen : TokenExpirationEvent
  data class Error(val errorCode: Int) : TokenExpirationEvent
}