package com.appmb.sdk.mbcore.platform

import androidx.datastore.preferences.core.stringPreferencesKey

interface MbDeviceInfo {
  suspend fun initDeviceId()
  suspend fun getDeviceId(): String

  companion object {
    val deviceIdKey = stringPreferencesKey("deviceId")
  }
}