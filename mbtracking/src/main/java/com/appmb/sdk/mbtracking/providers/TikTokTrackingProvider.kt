package com.appmb.sdk.mbtracking.providers

import android.content.Context
import android.util.Log
import com.appmb.sdk.mbtracking.TrackingEventData
import com.appmb.sdk.mbtracking.TrackingProvider
import com.appmb.sdk.mbtracking.TrackingProviderConfig
import com.appmb.sdk.mbtracking.TrackingProviderType
import com.tiktok.TikTokBusinessSdk
import org.json.JSONObject

class TikTokTrackingProvider : TrackingProvider {
    override val type: TrackingProviderType = TrackingProviderType.TIKTOK
    private var initialized = false

    override fun initialize(context: Context, config: TrackingProviderConfig) {
        val appId = config.tiktokAppId
        if (appId.isNullOrBlank()) {
            Log.e("TikTokTracking", "TikTok App ID is missing. TikTok tracking disabled.")
            return
        }

        val tiktokConfig = TikTokBusinessSdk.TTConfig(context, appId)
        // You can add more config here if needed, like:
        // tiktokConfig.disableAutoEvents()
        
        TikTokBusinessSdk.initializeSdk(tiktokConfig)
        initialized = true
        Log.i("TikTokTracking", "Initialized TikTok Business SDK with AppID: $appId")
    }

    override fun track(event: TrackingEventData) {
        if (!initialized) return
        
        // TikTok has standard events, but we can track custom ones too.
        // For simplicity, we map everything to event tracking.
        val nonNullProperties = event.properties.filterValues { it != null }
        val jsonProps = JSONObject()
        nonNullProperties.forEach { (key, value) ->
            jsonProps.put(key, value)
        }
        // Using deprecated trackEvent(String, JSONObject) as it's the most direct mapping
        // for arbitrary properties map. In future, migrate to trackTTEvent(TTEvent).
        @Suppress("DEPRECATION")
        TikTokBusinessSdk.trackEvent(event.name, jsonProps)
        Log.d("TikTokTracking", "track event=${event.name} properties=${event.properties}")
    }

    override fun setUserId(userId: String?) {
        // TikTok SDK usually tracks via TTIdentifier or other identifiers
        // but we can pass it if we have specific mapping.
    }

    override fun setUserProperties(properties: Map<String, Any?>) {
        // TikTok doesn't have a direct "setUserProperties" like Firebase,
        // but we can include these in events or use specific identifiers.
    }

    override fun logScreen(screenName: String, properties: Map<String, Any?>) {
        track(TrackingEventData("screen_view", properties + mapOf("screen_name" to screenName)))
    }

    override fun flush() {
        // TikTok SDK manages its own queue
    }
}
