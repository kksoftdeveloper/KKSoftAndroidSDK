package com.appmb.sdk.mbtracking.providers

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.appmb.sdk.mbtracking.TrackingEventData
import com.appmb.sdk.mbtracking.TrackingProvider
import com.appmb.sdk.mbtracking.TrackingProviderConfig
import com.appmb.sdk.mbtracking.TrackingProviderType
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class FacebookTrackingProvider : TrackingProvider {
  override val type: TrackingProviderType = TrackingProviderType.FACEBOOK
  private var logger: AppEventsLogger? = null

  override fun initialize(context: Context, config: TrackingProviderConfig) {
    val appId = config.facebookAppId
    val clientToken = config.facebookClientToken
    val displayName = config.facebookDisplayName

    if (appId.isNullOrBlank()) {
      Log.w("TrackingSDK-Meta", "Facebook App ID is blank. Skipping initialization.")
      return
    }

    FacebookSdk.setApplicationId(appId)
    if (!clientToken.isNullOrBlank()) {
        FacebookSdk.setClientToken(clientToken)
    }
    if (!displayName.isNullOrBlank()) {
        FacebookSdk.setApplicationName(displayName)
    }
    
    // FacebookSdk.sdkInitialize(context.applicationContext) // Usually auto-initialized or handled by provider
    
    logger = AppEventsLogger.newLogger(context.applicationContext)
    Log.i("TrackingSDK-Meta", "Initialized Facebook AppEventsLogger with AppID: $appId")
  }

  override fun track(event: TrackingEventData) {
    val instance = logger ?: return
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
    Log.d("TrackingSDK-Meta", "track event=${event.name} properties=${event.properties}")
    instance.logEvent(event.name, bundle)
  }

  override fun setUserId(userId: String?) {
    Log.d("TrackingSDK-Meta", "setUserId id=$userId")
    AppEventsLogger.setUserID(userId)
  }

  override fun setUserProperties(properties: Map<String, Any?>) {
    Log.d("TrackingSDK-Meta", "setUserProperties (not directly supported by Facebook AppEventsLogger, using UserData)")
    // Facebook uses setUpdateUserProperties for limited set or AppEventsLogger.setUserData
  }

  override fun logScreen(screenName: String, properties: Map<String, Any?>) {
    Log.d("TrackingSDK-Meta", "logScreen screen=$screenName properties=$properties")
    track(
      TrackingEventData(
        name = "fb_mobile_content_view",
        properties = properties + mapOf("fb_content_type" to "screen", "fb_content_id" to screenName)
      )
    )
  }

  override fun flush() {
    logger?.flush()
  }
}
