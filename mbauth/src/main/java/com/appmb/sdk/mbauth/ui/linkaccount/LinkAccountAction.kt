package com.appmb.sdk.mbauth.ui.linkaccount

import android.content.Context
import com.appmb.sdk.mbauth.model.LinkAccountType
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed interface LinkAccountAction {
  // Link Social Account
  data class LinkSocialAccount(
    val context: Context,
    val linkAccountType: LinkAccountType,
    val googleSignInAccount: GoogleSignInAccount? = null,
  ) : LinkAccountAction
}