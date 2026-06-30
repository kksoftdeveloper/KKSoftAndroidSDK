package com.appmb.sdk.mbtracking.util

import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

object CarrierUtils {

  /**
   * Returns the current carrier name if available, otherwise an empty string.
   *
   * This method is defensive:
   * - Tries SubscriptionManager (API 22+) when possible
   * - Falls back to TelephonyManager.simOperatorName / networkOperatorName
   * - Never throws; returns empty string on failure
   *
   * Note: Some sources may require runtime permission READ_PHONE_STATE depending on device/OS.
   */
  @JvmStatic
  fun getCarrierName(context: Context): String {
    return runCatching {
      // Try SubscriptionManager first for multi-SIM devices
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        val subMgr = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        val activeSubs: List<SubscriptionInfo>? = runCatching { subMgr?.activeSubscriptionInfoList }.getOrNull()
        val nameFromSubs = activeSubs
          ?.firstOrNull { it.carrierName != null && it.carrierName.isNotBlank() }
          ?.carrierName
          ?.toString()
        if (!nameFromSubs.isNullOrBlank()) {
          return nameFromSubs
        }
      }

      // Fallback to TelephonyManager
      val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
      val simName = telephony?.simOperatorName?.orEmpty().orEmpty()
      if (simName.isNotBlank()) return simName

      val netName = telephony?.networkOperatorName?.orEmpty().orEmpty()
      if (netName.isNotBlank()) return netName

      ""
    }.getOrElse { "" }
  }
}


