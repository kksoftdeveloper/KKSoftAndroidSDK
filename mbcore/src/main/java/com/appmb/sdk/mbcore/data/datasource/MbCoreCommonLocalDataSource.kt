package com.appmb.sdk.mbcore.data.datasource

import com.appmb.sdk.mbcore.data.datasource.facebookapp.FacebookApp
import com.appmb.sdk.mbcore.data.datasource.gapp.GApp
import com.appmb.sdk.mbcore.datastore.DataStoreManager
import com.appmb.sdk.mbcore.platform.MbDeviceInfo
import com.appmb.sdk.mbcore.utils.empty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import com.appmb.sdk.mbcore.BuildConfig as CoreBuildConfig

class MbCoreCommonLocalDataSource(
  val deviceInfo: MbDeviceInfo,
  val dataStoreManager: DataStoreManager,
  val facebookApp: FacebookApp,
  val gApp: GApp
) : MbCoreCommonDataSource, MbDeviceInfo by deviceInfo, GApp by gApp, FacebookApp by facebookApp {

  override fun getPlatform(): String {
    return CoreBuildConfig.PLATFORM.lowercase()
  }

  override fun getDeviceSecretId(): String {
    return CoreBuildConfig.DEVICE_SECRET_ID
  }

  override suspend fun getGameId(): String {
    return dataStoreManager.getPreference(
      MbCoreCommonDataSource.Companion.gameIdKey,
      String.empty()
    ).firstOrNull().orEmpty()
  }

  override suspend fun saveGameId(gameId: String) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.gameIdKey,
      gameId
    )
  }

  override suspend fun getServerId(): String? {
    return dataStoreManager.getPreference(
      MbCoreCommonDataSource.Companion.serverIdKey,
      null
    ).firstOrNull()
  }

  override suspend fun saveServerId(serverId: String?) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.serverIdKey,
      serverId
    )
  }

  override suspend fun getServerName(): String {
    return dataStoreManager.getPreference(
      MbCoreCommonDataSource.Companion.serverNameKey,
      String.empty()
    ).firstOrNull().orEmpty()
  }

  override suspend fun saveServerName(serverName: String?) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.serverNameKey,
      serverName
    )
  }

  override suspend fun getCharacterId(): String {
    return dataStoreManager.getPreference(
      MbCoreCommonDataSource.Companion.characterIdKey,
      String.empty()
    ).firstOrNull().orEmpty()
  }

  override suspend fun saveCharacterId(characterId: String) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.characterIdKey,
      characterId
    )
  }

  override suspend fun saveTimeRemaining(time: String) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.timeRemaining,
      time
    )
  }

  override suspend fun getTimeRemaining(): Long {
    return dataStoreManager
      .getPreference(
        MbCoreCommonDataSource.Companion.timeRemaining,
        String.empty()
      )
      .first()?.toLongOrNull() ?: 0
  }

  override suspend fun saveGuestLoginStartTime(timestamp: Long) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.guestLoginStartTime,
      timestamp.toString()
    )
  }

  override suspend fun getGuestLoginStartTime(): Long {
    return dataStoreManager
      .getPreference(
        MbCoreCommonDataSource.Companion.guestLoginStartTime,
        "0"
      )
      .first()?.toLongOrNull() ?: 0
  }

  override suspend fun isGuestUser(): Boolean {
    return dataStoreManager
      .getPreference(
        MbCoreCommonDataSource.Companion.isGuestUser,
        "false"
      )
      .first().toBoolean()
  }

  override suspend fun saveIsGuestUser(isGuest: Boolean) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.isGuestUser,
      isGuest.toString()
    )
  }

  override suspend fun isForceUpdateRequired(): Boolean {
    return dataStoreManager
      .getPreference(
        MbCoreCommonDataSource.Companion.isForceUpdateRequired,
        "false"
      )
      .first().toBoolean()
  }

  override suspend fun saveForceUpdateRequired(isForceUpdate: Boolean) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.isForceUpdateRequired,
      isForceUpdate.toString()
    )
  }

  override suspend fun isUserBlocked(): Boolean {
    return dataStoreManager
      .getPreference(
        MbCoreCommonDataSource.Companion.isUserBlocked,
        "false"
      )
      .first().toBoolean()
  }

  override suspend fun saveUserBlocked(userBlocked: Boolean) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.isUserBlocked,
      userBlocked.toString()
    )
  }

  override suspend fun saveAdid(adid: String?) {
    dataStoreManager.putPreference(
      MbCoreCommonDataSource.Companion.adidKey,
      adid.orEmpty()
    )
  }

  override suspend fun getAdid(): String? {
    return dataStoreManager.getPreference(
      MbCoreCommonDataSource.Companion.adidKey,
      String.empty()
    ).firstOrNull()?.takeIf { it.isNotEmpty() }
  }

  override suspend fun logout() {
    saveUserBlocked(false)
    saveIsGuestUser(false)
    saveForceUpdateRequired(false)
    saveCharacterId("")
    saveServerId(null)
    saveServerName(null)
    saveTimeRemaining("0")
    saveGuestLoginStartTime(0)
  }
}