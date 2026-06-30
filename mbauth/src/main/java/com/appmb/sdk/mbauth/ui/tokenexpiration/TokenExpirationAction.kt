package com.appmb.sdk.mbauth.ui.tokenexpiration

sealed interface TokenExpirationAction {
  object OnClickConfirmLogin: TokenExpirationAction
}