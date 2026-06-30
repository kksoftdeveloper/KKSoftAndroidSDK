package com.appmb.sdk.mbtracking

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.appmb.sdk.mbtracking.providers.AdjustTrackingProvider
import com.appmb.sdk.mbtracking.util.CarrierUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TrackingSdk(
  private val context: Context,
  private val providers: List<TrackingProvider>,
  private val config: TrackingProviderConfig = TrackingProviderConfig(),
  private val onAdidReady: ((String?) -> Unit)? = null
) {
  private val TAG = "TrackingSdk"
  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  
  fun initialize() {
    providers.forEach { it.initialize(context, config) }
    logAdjustIdsAfterInit()
  }

  private fun logAdjustIdsAfterInit() {
    // Delay to ensure Adjust ID is generated after initialization
    Handler(Looper.getMainLooper()).postDelayed({
      // Get Adjust ID using callback
      getAdjustId { adjustId ->
        if (adjustId != null) {
          Log.d(TAG, "[TESTING] Adjust ID: $adjustId | Use this ID in Adjust Dashboard to test events")
        } else {
          Log.w(TAG, "[TESTING] Adjust ID: Not available (Adjust may not be initialized or ID not ready yet)")
        }
        onAdidReady?.invoke(adjustId)
      }
      
      // Get and log Google Advertising ID asynchronously
      coroutineScope.launch {
        val advertisingId = getAdvertisingId()
        if (advertisingId != null) {
          Log.d(TAG, "[TESTING] Google Advertising ID (GAID): $advertisingId | Use this ID in Adjust Dashboard to test events")
        } else {
          Log.w(TAG, "[TESTING] Google Advertising ID: Not available (may require user consent or Google Play Services)")
        }
      }
    }, 2000) // 2 second delay to ensure Adjust ID is available
  }

  fun track(event: TrackingEvent) {
    providers.forEach { provider ->
      val payload = event.payloadFor(provider.type)
      provider.track(payload)
    }
  }
  
  /**
   * Enriches the tracking event with mobile carrier information.
   * Adds af_mobile_carrier to all event properties (default and overrides).
   */
  private fun enrichEventWithMobileCarrier(event: TrackingEvent): TrackingEvent {
    val mobileCarrier = getMobileCarrier()
    
    // Enrich default data
    val enrichedDefaultData = event.defaultData.copy(
      properties = event.defaultData.properties + ("af_mobile_carrier" to mobileCarrier)
    )
    
    // Enrich all overrides
    val enrichedOverrides = event.overrides.mapValues { (_, eventData) ->
      eventData.copy(
        properties = eventData.properties + ("af_mobile_carrier" to mobileCarrier)
      )
    }
    
    return event.copy(
      defaultData = enrichedDefaultData,
      overrides = enrichedOverrides
    )
  }

  /**
   * Gets the mobile carrier name from the device.
   * Returns the carrier name or "unknown" if not available.
   */
  fun getMobileCarrier(): String {
    return try {
      val carrierName = CarrierUtils.getCarrierName(context)
      if (carrierName.isNotBlank()) {
        carrierName
      } else {
        "unknown"
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to get mobile carrier: ${e.message}")
      "unknown"
    }
  }

  fun track(eventName: String, properties: Map<String, Any?> = emptyMap()) {
    track(TrackingEvent(TrackingEventData(eventName, properties)))
  }

  fun setUserId(userId: String?) {
    providers.forEach { it.setUserId(userId) }
  }

  fun setUserProperties(properties: Map<String, Any?>) {
    providers.forEach { it.setUserProperties(properties) }
  }

  fun logScreen(screenName: String, properties: Map<String, Any?> = emptyMap()) {
    providers.forEach { it.logScreen(screenName, properties) }
  }

  fun flush() {
    providers.forEach { it.flush() }
  }

  /**
   * Gets the Adjust ID for testing purposes using callback.
   * This ID can be used in the Adjust dashboard to test events.
   * @param callback Callback that receives the Adjust ID (or null if not available)
   */
  fun getAdjustId(callback: (String?) -> Unit) {
    val adjustProvider = providers.find { it is AdjustTrackingProvider } as? AdjustTrackingProvider
    if (adjustProvider != null) {
      adjustProvider.getAdjustId(callback)
    } else {
      callback(null)
    }
  }
  
  /**
   * Gets the Google Advertising ID (GAID) for testing purposes.
   * This ID can be used in the Adjust dashboard to test events.
   * Note: This requires Google Play Services and may require user consent.
   * @return The Google Advertising ID, or null if not available
   */
  suspend fun getAdvertisingId(): String? {
    val adjustProvider = providers.find { it is AdjustTrackingProvider } as? AdjustTrackingProvider
    return adjustProvider?.getAdvertisingId()
  }
}


