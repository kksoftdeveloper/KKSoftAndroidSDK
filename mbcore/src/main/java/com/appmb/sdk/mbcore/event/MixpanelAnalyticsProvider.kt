package com.appmb.sdk.mbcore.event

import android.content.Context
import com.appmb.sdk.mbcore.BuildConfig
import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import org.json.JSONObject

class MixpanelAnalyticsProvider(
  private val context: Context,
  private val token: String,
) : AnalyticsProvider {

  override fun trackEvent(eventName: String, properties: JSONObject) {
    val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(context, token, true)
    // Normalize event name to remove base URLs
    val normalizedEventName = SensitiveDataNormalizer.normalizeEventName(eventName)
    // Normalize sensitive data before sending to Mixpanel
    val normalizedProperties = SensitiveDataNormalizer.normalizeJsonObject(properties)
    mixpanel.track(normalizedEventName, normalizedProperties)
    mixpanel.flush()
  }

  override fun trackMap(eventName: String, properties: Map<String, Any?>) {
    val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(context, token, true)
    // Normalize event name to remove base URLs
    val normalizedEventName = SensitiveDataNormalizer.normalizeEventName(eventName)
    // Normalize sensitive data before sending to Mixpanel
    val normalizedProperties = SensitiveDataNormalizer.normalizeMap(properties)
    mixpanel.trackMap(normalizedEventName, normalizedProperties)
    mixpanel.flush()
  }

  override fun flush() {
    val mixpanel: MixpanelAPI = MixpanelAPI.getInstance(context, token, true)
    mixpanel.flush()
  }
}
