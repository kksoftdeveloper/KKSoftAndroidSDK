package com.appmb.sdk.mbcore.data.datasource.facebookapp

import androidx.datastore.preferences.core.stringPreferencesKey
import com.appmb.sdk.mbcore.datastore.DataStoreManager
import com.appmb.sdk.mbcore.utils.empty
import kotlinx.coroutines.flow.first

interface FacebookApp {
  suspend fun saveFacebookAppId(appId: String)
  suspend fun getFacebookAppId(): String?

  suspend fun saveFacebookClientToken(clientToken: String)
  suspend fun getFacebookClientToken(): String?
}

class FacebookAppImpl(
  private val dataStoreManager: DataStoreManager
) : FacebookApp {

  override suspend fun saveFacebookAppId(appId: String) {
    dataStoreManager.putPreference(
      facebookAppIdKey,
      appId
    )
  }

  override suspend fun getFacebookAppId(): String? {
    return dataStoreManager.getPreference(
      facebookAppIdKey,
      null
    ).first()
  }

  override suspend fun saveFacebookClientToken(clientToken: String) {
    dataStoreManager.putPreference(
      facebookClientTokenKey,
      clientToken
    )
  }

  override suspend fun getFacebookClientToken(): String? {
    return dataStoreManager.getPreference(
      facebookClientTokenKey,
      null
    ).first()
  }

  companion object {
    private val facebookAppIdKey = stringPreferencesKey("facebookAppIdKey")
    private val facebookClientTokenKey = stringPreferencesKey("facebookClientTokenKey")
  }

}