package com.appmb.sdk.mbauth.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class SdkParams : Parcelable {

  @Parcelize
  data class Authenticate(val gameId: Int) : SdkParams()

  @Parcelize
  object Logout : SdkParams()

  @Parcelize
  object LinkAccount : SdkParams()

  @Parcelize
  object UpdateApp : SdkParams()

  @Parcelize
  data class ChooseServer(val isEnableClose: Boolean) : SdkParams()

  @Parcelize
  object DeactivateAccount : SdkParams()

  @Parcelize
  object TokenExpiration : SdkParams()

  @Parcelize
  object UserBlocked : SdkParams()

  @Parcelize
  object ServerMaintenance : SdkParams()
}
