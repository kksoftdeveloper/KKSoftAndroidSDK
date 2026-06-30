package com.appmb.sdk.mbcore.event

import org.json.JSONObject

interface AnalyticsProvider {
  fun trackEvent(eventName: String, properties: JSONObject)
  fun trackMap(eventName: String, properties: Map<String, Any?>)
  fun flush()
}