package com.appmb.sdk.mbcore.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.config.MbSdkConfig

object VersionInfo {
  var isForceUpdate: Boolean = false
  var minVersion: String? = null
  var alreadyDisplayRequireUpdatePopup = false

  private val mbSdkConfig = MbSdk.getKoin().get<MbSdkConfig>()

  fun isOutDatedVersion(): Boolean {
//    try {
//      val currentVersion = mbSdkConfig.getAppVersionName()?.split(".").orEmpty()
//      val requireVersion = minVersion?.split(".").orEmpty()
//      val len = maxOf(currentVersion.size, requireVersion.size)
//      for (i in 0 until len) {
//        val p1 = currentVersion.getOrNull(i)?.toIntOrNull() ?: 0
//        val p2 = requireVersion.getOrNull(i)?.toIntOrNull() ?: 0
//        if (p1 != p2) return p1 < p2
//      }
//      return false
//
//    } catch (e: Exception) {
//      return false
//    }
    return isForceUpdate
  }

  fun openPlayStore(context: Context) {
    val playStoreIntent = Intent(
      Intent.ACTION_VIEW,
      "market://details?id=${mbSdkConfig.getAppId()}".toUri()
    ).apply {
      setPackage("com.android.vending")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
      context.startActivity(playStoreIntent)
    } catch (e: ActivityNotFoundException) {
      val webIntent = Intent(
        Intent.ACTION_VIEW,
        "https://play.google.com/store/apps/details?id=${mbSdkConfig.getAppId()}".toUri()
      ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(webIntent)
    }
  }
}