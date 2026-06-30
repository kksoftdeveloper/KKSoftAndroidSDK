package com.appmb.sdk.mbpayment

import android.app.Activity
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.model.isNullOrEmpty
import com.appmb.sdk.mbpayment.di.MbPaymentKoin
import com.appmb.sdk.mbpayment.model.PurchaseResult
import com.appmb.sdk.mbpayment.network.MbPaymentApi

object MbPayment {
  const val EXTRA_PURCHASE_RESULT: String = "purchase_result"
  const val ACTION_PURCHASE_DONE: String = "ACTION_PURCHASE_DONE"

  fun init() {
    MbPaymentKoin.loadModule()
    MbSdk.getConfig().setPaymentSdkVersion(BuildConfig.LIBRARY_VERSION)

  }
  @JvmStatic
  fun startPayment(
    activity: Activity
  ) {
    val sessionData = MbSdk.getCurrentSessionData()
    if (!sessionData.isNullOrEmpty()) {
      val intent = Intent(activity, PaymentActivity::class.java)
      activity.startActivity(intent)
    } else {
      val resultIntent = Intent(MbPayment.ACTION_PURCHASE_DONE).apply {
        putExtra(EXTRA_PURCHASE_RESULT, PurchaseResult.PurchasedUserNotAuthenticated)
      }
      LocalBroadcastManager.getInstance(activity).sendBroadcast(resultIntent)
    }
  }
}