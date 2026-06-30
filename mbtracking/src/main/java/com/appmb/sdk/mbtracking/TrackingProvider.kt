package com.appmb.sdk.mbtracking

import android.content.Context

interface TrackingProvider {
  val type: TrackingProviderType
  fun initialize(context: Context, config: TrackingProviderConfig = TrackingProviderConfig())
  fun track(event: TrackingEventData)
  fun setUserId(userId: String?)
  fun setUserProperties(properties: Map<String, Any?>)
  fun logScreen(screenName: String, properties: Map<String, Any?> = emptyMap())
  fun flush()
}

data class TrackingProviderConfig(
  val adjustAppToken: String? = null,
  val tiktokAppId: String? = null,
  val facebookAppId: String? = null,
  val facebookClientToken: String? = null,
  val facebookDisplayName: String? = null,
  val firebaseAppId: String? = null,
  val gidClientID: String? = null,
  val appFlyersId: String? = null,
  val appFlyersDevKey: String? = null,
)


