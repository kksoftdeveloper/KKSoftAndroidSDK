package com.appmb.sdk.mbtracking

data class TrackingEventData(
  val name: String,
  val properties: Map<String, Any?> = emptyMap()
)

data class TrackingEvent(
  val defaultData: TrackingEventData,
  val overrides: Map<TrackingProviderType, TrackingEventData> = emptyMap()
) {
  fun payloadFor(type: TrackingProviderType): TrackingEventData {
    return overrides[type] ?: defaultData
  }
}

