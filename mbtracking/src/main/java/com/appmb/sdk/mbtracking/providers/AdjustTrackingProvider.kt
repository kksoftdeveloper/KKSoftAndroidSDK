package com.appmb.sdk.mbtracking.providers

import android.content.Context
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.LogLevel
import com.adjust.sdk.OnAdidReadListener
import com.appmb.sdk.mbtracking.TrackingEventData
import com.appmb.sdk.mbtracking.TrackingProvider
import com.appmb.sdk.mbtracking.TrackingProviderConfig
import com.appmb.sdk.mbtracking.TrackingProviderType
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AdjustTrackingProvider : TrackingProvider {
  override val type: TrackingProviderType = TrackingProviderType.ADJUST
  private var isInitialized = false
  private var appContext: Context? = null
  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  override fun initialize(context: Context, config: TrackingProviderConfig) {
    if (isInitialized) return
    appContext = context.applicationContext
    val appToken = config.adjustAppToken.orEmpty()
    if (appToken.isBlank()) {
      Log.w("TrackingSDK-Adjust", "Adjust App Token is blank. Skipping initialization.")
      return
    }

    val packageName = context.packageName
    Log.d("TrackingSDK-Adjust", "Initializing Adjust with package: $packageName, appToken: ****${appToken.takeLast(4)}")

    // Determine environment (use production by default, can be configured)
    val environment = AdjustConfig.ENVIRONMENT_PRODUCTION
    
    val adjustConfig = AdjustConfig(context.applicationContext, appToken, environment)
    adjustConfig.setLogLevel(LogLevel.VERBOSE) // Enable verbose logging for debugging
    adjustConfig.enableSendingInBackground()

    Adjust.initSdk(adjustConfig)
    
    Log.i("TrackingSDK-Adjust", "Initialized with appToken: ****${appToken.takeLast(4)}")
    isInitialized = true
    
    // Log Adjust ID and Advertising ID after initialization (with a delay to ensure IDs are available)
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
      logAdjustIdsForTesting()
    }, 2000) // 2 second delay to ensure Adjust ID is available
  }
  
  /**
   * Logs Adjust ID and Google Advertising ID for testing purposes.
   * These IDs can be used in the Adjust dashboard to test events.
   */
  private fun logAdjustIdsForTesting() {
    // Get Adjust ID using callback
    getAdjustId { adjustId ->
      if (adjustId != null) {
        Log.d("TrackingSDK-Adjust", "[TESTING] Adjust ID: $adjustId | Use this ID in Adjust Dashboard to test events")
      } else {
        Log.w("TrackingSDK-Adjust", "[TESTING] Adjust ID: Not available (Adjust may not be initialized or ID not ready yet)")
      }
    }
    
    // Get and log Google Advertising ID asynchronously
    coroutineScope.launch {
      val advertisingId = getAdvertisingId()
      if (advertisingId != null) {
        Log.d("TrackingSDK-Adjust", "[TESTING] Google Advertising ID (GAID): $advertisingId | Use this ID in Adjust Dashboard to test events")
      } else {
        Log.w("TrackingSDK-Adjust", "[TESTING] Google Advertising ID: Not available (may require user consent or Google Play Services)")
      }
    }
  }
  
  /**
   * Gets the Adjust ID for testing purposes using callback.
   * This ID can be used in the Adjust dashboard to test events.
   * Note: The ADID may not be available immediately after initialization.
   * It becomes available after the SDK tracks the install/session.
   * @param callback Callback that receives the Adjust ID (or null if not available)
   */
  fun getAdjustId(callback: (String?) -> Unit) {
    if (appContext == null) {
      callback(null)
      return
    }
    try {
      Adjust.getAdid(object : OnAdidReadListener {
        override fun onAdidRead(adid: String?) {
          if (adid != null) {
            Log.d("TrackingSDK-Adjust", "Adjust ID: $adid")
          }
          callback(adid)
        }
      })
    } catch (e: Exception) {
      Log.w("TrackingSDK-Adjust", "Failed to get Adjust ID: ${e.message}")
      callback(null)
    }
  }
  
  /**
   * Gets the Adjust ID asynchronously with a timeout.
   * This is more reliable as it waits for the ADID to become available.
   * @param timeoutMs Timeout in milliseconds (default: 5000ms)
   * @param callback Callback that receives the Adjust ID (or null if timeout/error)
   */
  fun getAdjustIdWithTimeout(
    timeoutMs: Long = 5000L,
    callback: (String?) -> Unit
  ) {
    val ctx = appContext
    if (ctx == null) {
      callback(null)
      return
    }
    try {
      Adjust.getAdidWithTimeout(ctx, timeoutMs, object : OnAdidReadListener {
        override fun onAdidRead(adid: String?) {
          if (adid != null) {
            Log.d("TrackingSDK-Adjust", "Adjust ID (with timeout): $adid")
          }
          callback(adid)
        }
      })
    } catch (e: Exception) {
      Log.w("TrackingSDK-Adjust", "Failed to get Adjust ID with timeout: ${e.message}")
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
    val ctx = appContext ?: return null
    return try {
      val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(ctx)
      val advertisingId = adInfo?.id
      if (advertisingId != null) {
        Log.d("TrackingSDK-Adjust", "Google Advertising ID: $advertisingId")
      }
      advertisingId
    } catch (e: Exception) {
      Log.w("TrackingSDK-Adjust", "Failed to get Google Advertising ID: ${e.message}")
      null
    }
  }

  override fun track(event: TrackingEventData) {
    if (!isInitialized) return
    
    // Use event name with adj_ prefix as event token
    // Note: In production, you should map event names to actual Adjust event tokens
    val eventToken = event.name
    
    try {
      val adjustEvent = AdjustEvent(eventToken)
      
      // Add all properties as callback parameters with adj_ prefix
      event.properties.forEach { (key, value) ->
        val adjKey = if (key.startsWith("adj_")) key else "adj_$key"
        val stringValue = value?.toString() ?: ""
        adjustEvent.addCallbackParameter(adjKey, stringValue)
      }
      
      Log.d("TrackingSDK-Adjust", "track event=$eventToken properties=${event.properties}")
      Adjust.trackEvent(adjustEvent)
    } catch (e: Exception) {
      Log.e("TrackingSDK-Adjust", "Failed to track event: ${event.name}", e)
    }
  }

  override fun setUserId(userId: String?) {
    Log.d("TrackingSDK-Adjust", "setUserId id=$userId")
    // Adjust doesn't have a direct setUserId method, but we can add it as a callback parameter
    // Alternatively, you can use Adjust.addSessionCallbackParameter for persistent user ID
    if (userId != null) {
//      Adjust.addSessionCallbackParameter("adj_user_id", userId)
    }
  }

  override fun setUserProperties(properties: Map<String, Any?>) {
    Log.d("TrackingSDK-Adjust", "setUserProperties properties=$properties")
    // Add user properties as session callback parameters with adj_ prefix
    properties.forEach { (key, value) ->
      val adjKey = if (key.startsWith("adj_")) key else "adj_$key"
      val stringValue = value?.toString() ?: ""
//      Adjust.addSessionCallbackParameter(adjKey, stringValue)
    }
  }

  override fun logScreen(screenName: String, properties: Map<String, Any?>) {
    Log.d("TrackingSDK-Adjust", "logScreen screen=$screenName properties=$properties")
    track(
      TrackingEventData(
        name = "adj_screen_view",
        properties = properties + mapOf("adj_screen_name" to screenName)
      )
    )
  }

  override fun flush() {
    // Adjust flushes automatically, but we can trigger it manually if needed
    Log.d("TrackingSDK-Adjust", "flush (auto-managed)")
  }
}
