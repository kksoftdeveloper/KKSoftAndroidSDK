package com.appmb.sdk.mbcore.config

data class TrackingConfig(
  val enableFirebase: Boolean = false,
  val enableAdjust: Boolean = false,
  val enableTikTok: Boolean = false,
  val enableMeTa: Boolean = false,
  val enableAppFlyers: Boolean = false,
  val gidClientID: String? = null,

  val facebookAppID: String? = null,
  val facebookClientToken: String? = null,
  val facebookDisplayName: String? = null,

  val firebaseAppID: String? = null,

  val adjustId: String? = null,
  val adjustToken: String? = null,

  val appFlyersId: String? = null,
  val appFlyersDevKey: String? = null,

  val tiktokAppID: String? = null,
  val tiktokAccessToken: String? = null
) {
  class Builder {
    private var enableFirebase: Boolean = false
    private var enableAdjust: Boolean = false
    private var enableTikTok: Boolean = false
    private var enableMeTa: Boolean = false
    private var enableAppFlyers: Boolean = false
    private var gidClientID: String? = null
    private var facebookAppID: String? = null
    private var facebookClientToken: String? = null
    private var facebookDisplayName: String? = null
    private var firebaseAppID: String? = null
    private var adjustId: String? = null
    private var adjustToken: String? = null
    private var appFlyersId: String? = null
    private var appFlyersDevKey: String? = null
    private var tiktokAppID: String? = null
    private var tiktokAccessToken: String? = null

    fun enableFirebase(enabled: Boolean, appId: String?) = apply {
      this.enableFirebase = enabled
      this.firebaseAppID = appId
    }

    fun enableAdjust(enabled: Boolean, id: String?, token: String?) = apply {
      this.enableAdjust = enabled
      this.adjustId = id
      this.adjustToken = token
    }

    fun enableTikTok(enabled: Boolean, appId: String?, accessToken: String?) = apply {
      this.enableTikTok = enabled
      this.tiktokAppID = appId
      this.tiktokAccessToken = accessToken
    }

    fun enableMeta(enabled: Boolean, appId: String?, clientToken: String?, displayName: String?) = apply {
      this.enableMeTa = enabled
      this.facebookAppID = appId
      this.facebookClientToken = clientToken
      this.facebookDisplayName = displayName
    }

    fun enableAppFlyers(enabled: Boolean, id: String?, devKey: String?) = apply {
      this.enableAppFlyers = enabled
      this.appFlyersId = id
      this.appFlyersDevKey = devKey
    }

    fun enableGid(clientId: String?) = apply {
      this.gidClientID = clientId
    }

    fun build() = TrackingConfig(
      enableFirebase = enableFirebase,
      enableAdjust = enableAdjust,
      enableTikTok = enableTikTok,
      enableMeTa = enableMeTa,
      enableAppFlyers = enableAppFlyers,
      gidClientID = gidClientID,
      facebookAppID = facebookAppID,
      facebookClientToken = facebookClientToken,
      facebookDisplayName = facebookDisplayName,
      firebaseAppID = firebaseAppID,
      adjustId = adjustId,
      adjustToken = adjustToken,
      appFlyersId = appFlyersId,
      appFlyersDevKey = appFlyersDevKey,
      tiktokAppID = tiktokAppID,
      tiktokAccessToken = tiktokAccessToken
    )
  }
}
