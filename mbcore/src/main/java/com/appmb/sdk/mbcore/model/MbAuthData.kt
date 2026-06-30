package com.appmb.sdk.mbcore.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
@Parcelize
data class MbAuthData(
  val accessToken: String? = null,
  val refreshToken: String? = null,
  val expireDate: String? = null,
  val serverId: String? = null,
  val gameUuid: String? = null,
  val userId: String? = null,
  val refreshExpireDate: String? = null,
  val isGuest: Boolean? = false,
  val userBlocked: Boolean? = false,
  val gameBlocked: Boolean? = false,
  val serverBlocked: Boolean? = false,
) : Parcelable

fun MbAuthData?.isNullOrEmpty(): Boolean {
  return this?.accessToken.isNullOrEmpty() || this?.refreshToken.isNullOrEmpty() || this?.expireDate.isNullOrEmpty()
}