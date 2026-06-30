package com.appmb.sdk.mbpayment.util

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

interface PlayServicesChecker {
    fun isGooglePlayServicesAvailable(context: Context): Boolean
}

class DefaultPlayServicesChecker : PlayServicesChecker {
    override fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
}

