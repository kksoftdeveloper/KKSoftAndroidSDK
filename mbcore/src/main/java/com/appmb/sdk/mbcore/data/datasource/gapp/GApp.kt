package com.appmb.sdk.mbcore.data.datasource.gapp

import androidx.datastore.preferences.core.stringPreferencesKey
import com.appmb.sdk.mbcore.datastore.DataStoreManager
import com.appmb.sdk.mbcore.utils.empty
import kotlinx.coroutines.flow.first

interface GApp {
  suspend fun saveGoogleClientId(clientId: String)
  suspend fun getGoogleClientId(): String?

  suspend fun saveGooglePlatformUrlSchema(platformUrlSchema: String)
  suspend fun getGooglePlatformUrlSchema(): String?
}

class GAppImpl(
  private val dataStoreManager: DataStoreManager
) : GApp {

  override suspend fun saveGoogleClientId(clientId: String) {
    dataStoreManager.putPreference(
      googleClientIdKey,
      clientId
    )
  }

  override suspend fun getGoogleClientId(): String? {
    return dataStoreManager.getPreference(
      googleClientIdKey,
      null
    ).first()
  }

  override suspend fun saveGooglePlatformUrlSchema(platformUrlSchema: String) {
    dataStoreManager.putPreference(
      googlePlatformUrlSchemaKey,
      platformUrlSchema
    )
  }

  override suspend fun getGooglePlatformUrlSchema(): String? {
    return dataStoreManager.getPreference(
      googlePlatformUrlSchemaKey,
      null
    ).first()
  }

  companion object {
    private val googleClientIdKey = stringPreferencesKey("googleClientIdKey")
    private val googlePlatformUrlSchemaKey = stringPreferencesKey("googlePlatformUrlSchemaKey")
  }
}