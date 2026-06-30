package com.appmb.sdk.mbauth.ui.linkaccount

import com.appmb.sdk.mbauth.ui.login.AuthResult

sealed interface LinkAccountEvent {
  data class Error(val code: Int) : LinkAccountEvent
  data class LinkSuccess(val authResult: AuthResult.LinkAccount) : LinkAccountEvent
}