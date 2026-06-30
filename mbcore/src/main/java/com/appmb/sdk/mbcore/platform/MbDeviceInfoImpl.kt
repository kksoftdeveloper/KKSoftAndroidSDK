package com.appmb.sdk.mbcore.platform

import com.appmb.sdk.mbcore.datastore.DataStoreManager
import com.appmb.sdk.mbcore.utils.empty
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class MbDeviceInfoImpl(
  val dataStoreManager: DataStoreManager,
) : MbDeviceInfo {

  override suspend fun initDeviceId() {
    val deviceId = if (getDeviceId().isEmpty()) UUID.randomUUID().toString() else return
    dataStoreManager.putPreference(
      MbDeviceInfo.Companion.deviceIdKey,
      deviceId
    )
  }

  override suspend fun getDeviceId(): String {
    return dataStoreManager.getPreference(
      MbDeviceInfo.Companion.deviceIdKey,
      String.empty()
    ).firstOrNull().orEmpty()
  }

}