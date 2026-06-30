package com.appmb.sdk.mbauth.tracking

import android.content.Context
import android.util.Log
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbtracking.TrackingEvent
import com.appmb.sdk.mbtracking.TrackingEventData
import com.appmb.sdk.mbtracking.TrackingEvents
import com.appmb.sdk.mbtracking.TrackingProviderType
import com.appmb.sdk.mbtracking.TrackingSdk
import com.appmb.sdk.mbtracking.util.CarrierUtils
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import org.koin.java.KoinJavaComponent

internal object AuthTracking {

  private const val TAG = "AuthTracking"
  private const val PREFS_NAME = "auth_tracking_prefs"
  private const val FIRST_LOGIN_KEY_PREFIX = "auth_retention_first_login_"
  private const val PENDING_KEY_PREFIX = "auth_retention_d1_pending_"

  private val context: Context
    get() = MbSdk.getContext()

  private val prefs by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  private fun trackingManager(): TrackingSdk? =
    runCatching { KoinJavaComponent.getKoin().get<TrackingSdk>() }.getOrNull()

  /**
   * Calculates the 2 AM timestamp of the calendar day for the given timestamp.
   * @param timestampMillis The timestamp in milliseconds
   * @return The timestamp representing 2 AM of that day in the device's timezone
   */
  private fun get2AMTimestampOfDay(timestampMillis: Long): Long {
    val calendar = Calendar.getInstance(TimeZone.getDefault())
    calendar.timeInMillis = timestampMillis
    calendar.set(Calendar.HOUR_OF_DAY, 2)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
  }

  fun logOpenLoginForm() {
//    val afParams = mapOf(
//      "af_platform" to "android"
//    )
//    val fbParams = mapOf(
//      "platform" to "android"
//    )
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_OPEN_LOGIN_FORM,
//          properties = afParams
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_open_login_form",
//            properties = fbParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_open_login_form"
          )
        )
      )
    )
  }

  fun logLoginSuccess(
    method: String,
    session: MbAuthData
  ) {
    val carrier = CarrierUtils.getCarrierName(context).ifBlank { "--" }
    val params = mutableMapOf<String, Any?>(
      "af_login_method" to method,
      "af_mobile_carrier" to carrier
    )
    session.gameUuid?.let { uid ->
      params["af_uid"] = uid
      trackingManager()?.setUserId(uid)
    }
    val firebaseParams = mapOf(
      "user_id" to (session.gameUuid ?: ""),
      "method" to method,
//      "mobile_carrier" to carrier
    )
    val adjustParams = mutableMapOf<String, Any?>(
      "adj_login_method" to method,
      "adj_mobile_carrier" to carrier
    )
    session.gameUuid?.let { uid ->
      adjustParams["adj_uid"] = uid
    }
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_LOGIN,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_login",
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_login",
            properties = adjustParams
          )
        )
      )
    )
    handleRetentionD1IfNeeded(session)
  }

  fun logLoginFailure(
    method: String,
    error: NetworkError
  ) {
//    val carrier = CarrierUtils.getCarrierName(context).ifBlank { "--" }
    val params = mutableMapOf<String, Any?>(
      "af_login_method" to method,
//      "af_mobile_carrier" to carrier,
//      "af_reason" to error.javaClass.simpleName
    )
    when (error) {
      is NetworkError.ApiError -> {
        params["af_login_fail_reason"] = error.errorBody.message
      }

      else -> {
        params["af_login_fail_reason"] = "Unknown error"
      }
    }
    val firebaseParams = mapOf(
      "method" to method
    )
    val adjustParams = mutableMapOf<String, Any?>(
      "adj_login_method" to method
    )
    when (error) {
      is NetworkError.ApiError -> {
        adjustParams["adj_login_fail_reason"] = error.errorBody.message
      }
      else -> {
        adjustParams["adj_login_fail_reason"] = "Unknown error"
      }
    }
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_LOGIN_FAIL,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_login_fail",
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_login_fail",
            properties = adjustParams
          )
        )
      )
    )
  }

  fun logRegisterSuccess(
    method: String,
    session: MbAuthData
  ) {
    val carrier = CarrierUtils.getCarrierName(context).ifBlank { "--" }
    val params = mutableMapOf<String, Any?>(
      "af_signup_method" to method,
      "af_mobile_carrier" to carrier
    )
    session.gameUuid?.let { uid ->
      params["af_uid"] = uid
      trackingManager()?.setUserId(uid)
    }
    val firebaseParams = mapOf(
      "method" to method,
//      "mobile_carrier" to carrier,
      "user_id" to (session.gameUuid ?: "")
    )
    val adjustParams = mutableMapOf<String, Any?>(
      "adj_signup_method" to method,
      "adj_mobile_carrier" to carrier
    )
    session.gameUuid?.let { uid ->
      adjustParams["adj_uid"] = uid
    }
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_REGISTRATION,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_registration",
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_registration",
            properties = adjustParams
          )
        )
      )
    )
  }

  private fun handleRetentionD1IfNeeded(session: MbAuthData) {
    val uid = session.gameUuid ?: return
    val firstLoginKey = FIRST_LOGIN_KEY_PREFIX + uid
    val pendingKey = PENDING_KEY_PREFIX + uid
    val now = System.currentTimeMillis()

    if (!prefs.contains(firstLoginKey)) {
      // Calculate 2 AM of the first login day and store it
      val twoAMTimestamp = get2AMTimestampOfDay(now)
      prefs.edit()
        .putLong(firstLoginKey, twoAMTimestamp)
        .putBoolean(pendingKey, true)
        .apply()
      return
    }

    val isPending = prefs.getBoolean(pendingKey, true)
    if (!isPending) return

    // Get the 2 AM timestamp of the first login day
    val firstLogin2AMTime = prefs.getLong(firstLoginKey, now)
    val elapsed = now - firstLogin2AMTime
    val oneDay = TimeUnit.DAYS.toMillis(1)

    if (elapsed in oneDay until (oneDay * 2)) {
      val carrier = CarrierUtils.getCarrierName(context).ifBlank { "--" }
      val params = mapOf(
        "af_uid" to uid,
        "af_retention_days" to 1,
        "af_mobile_carrier" to carrier
      )
      val firebaseParams = mapOf(
        "user_id" to uid,
        "retention_days" to 1,
        "mobile_carrier" to carrier
      )
      val adjustParams = mapOf(
        "adj_uid" to uid,
        "adj_retention_days" to 1,
        "adj_mobile_carrier" to carrier
      )
      Log.d(TAG, "[AuthTracking] Retention D1 triggered for $uid")
      trackingManager()?.track(
        TrackingEvent(
          defaultData = TrackingEventData(
            name = TrackingEvents.AF_RETENTION_D1,
//            properties = params
          ),
          overrides = mapOf(
            TrackingProviderType.FIREBASE to TrackingEventData(
              name = "fb_retention_d1",
//              properties = firebaseParams
            ),
            TrackingProviderType.ADJUST to TrackingEventData(
              name = "adj_retention_d1",
              properties = adjustParams
            )
          )
        )
      )
      prefs.edit().putBoolean(pendingKey, false).apply()
    } else if (elapsed >= (oneDay * 2)) {
      prefs.edit().putBoolean(pendingKey, false).apply()
    }
  }
}


