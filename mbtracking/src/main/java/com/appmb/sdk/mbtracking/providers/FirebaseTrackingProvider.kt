package com.appmb.sdk.mbtracking.providers

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.appmb.sdk.mbtracking.TrackingEventData
import com.appmb.sdk.mbtracking.TrackingProvider
import com.appmb.sdk.mbtracking.TrackingProviderConfig
import com.appmb.sdk.mbtracking.TrackingProviderType
import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.collections.plus

class FirebaseTrackingProvider : TrackingProvider {
  override val type: TrackingProviderType = TrackingProviderType.FIREBASE
  private var firebase: FirebaseAnalytics? = null

  override fun initialize(context: Context, config: TrackingProviderConfig) {
    firebase = FirebaseAnalytics.getInstance(context.applicationContext)
    Log.i("TrackingSDK-Firebase", "Initialized FirebaseAnalytics")
  }

  override fun track(event: TrackingEventData) {
    val instance = firebase ?: return
    val bundle = Bundle().apply {
      event.properties.forEach { (k, v) ->
        when (v) {
          is String -> putString(k, v)
          is Int -> putInt(k, v)
          is Long -> putLong(k, v)
          is Double -> putDouble(k, v)
          is Float -> putDouble(k, v.toDouble())
          is Boolean -> putString(k, v.toString())
          else -> if (v != null) putString(k, v.toString())
        }
      }
    }
    Log.d("TrackingSDK-Firebase", "track event=${event.name} properties=${event.properties}")
    instance.logEvent(event.name, bundle)
  }

  override fun setUserId(userId: String?) {
    Log.d("TrackingSDK-Firebase", "setUserId id=$userId")
    firebase?.setUserId(userId)
  }

  override fun setUserProperties(properties: Map<String, Any?>) {
    Log.d("TrackingSDK-Firebase", "setUserProperties properties=$properties")
    properties.forEach { (key, value) ->
      firebase?.setUserProperty(key, value?.toString())
    }
  }

  override fun logScreen(screenName: String, properties: Map<String, Any?>) {
    Log.d("TrackingSDK-Firebase", "logScreen screen=$screenName properties=$properties")
    track(
      TrackingEventData(
        name = "screen_view",
        properties = properties + mapOf("screen_name" to screenName)
      )
    )
  }

  override fun flush() {
    // Firebase flushes automatically
    Log.d("TrackingSDK-Firebase", "flush (auto-managed)")
  }

}


