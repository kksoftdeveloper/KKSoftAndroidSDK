package com.appmb.sdk.mbauth.ui.linkaccount

import com.appmb.sdk.mbauth.ui.login.AuthResult

data class LinkAccountState(
  val isLoading: Boolean = false,
  val error: Int? = null,
  val authResult: AuthResult? = null
)