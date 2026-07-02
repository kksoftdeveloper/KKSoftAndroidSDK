package com.appmb.sdk.mbauth.ui.login

import android.os.Parcelable
import com.appmb.sdk.mbcore.model.MbAuthData
import kotlinx.parcelize.Parcelize

sealed class AuthResult : Parcelable {
  @Parcelize
  data class AuthSuccess(
    val user: MbAuthData,
    val isSocialLogin: Boolean = false,
  ) : AuthResult()

  @Parcelize
  data class RegisterSuccess(val user: MbAuthData) : AuthResult()

  @Parcelize
  data class Failure(val status: Int, val msg: String) : AuthResult()

  @Parcelize
  data class Logout(val isLogoutSuccess: Boolean) : AuthResult()

  @Parcelize
  data class LinkAccount(val user: MbAuthData) : AuthResult()

  @Parcelize
  data class RepeatableRemindLinkAccount(val isRepeated: Boolean) : AuthResult()

  @Parcelize
  data class DeactivateAccount(val isSuccess: Boolean) : AuthResult()

  @Parcelize
  data class ResetPassword(val status: Int, val message: String) : AuthResult()

  @Parcelize
  data class SelectedServerGame(val serverId: Int?) : AuthResult()
}
