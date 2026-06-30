package com.appmb.sdk.mbcore.data.datasource

import androidx.datastore.preferences.core.stringPreferencesKey
import com.appmb.sdk.mbcore.data.datasource.facebookapp.FacebookApp
import com.appmb.sdk.mbcore.data.datasource.gapp.GApp
import com.appmb.sdk.mbcore.platform.MbDeviceInfo

interface MbCoreCommonDataSource : MbDeviceInfo, FacebookApp, GApp {
  fun getPlatform(): String
  fun getDeviceSecretId(): String
  suspend fun getGameId(): String
  suspend fun saveGameId(gameId: String)
  suspend fun getServerId(): String?
  suspend fun saveServerId(serverId: String?)
  suspend fun getServerName(): String
  suspend fun saveServerName(serverName: String?)
  suspend fun getCharacterId(): String
  suspend fun saveCharacterId(characterId: String)
  suspend fun saveTimeRemaining(time: String)
  suspend fun getTimeRemaining(): Long
  suspend fun saveGuestLoginStartTime(timestamp: Long)
  suspend fun getGuestLoginStartTime(): Long
  suspend fun isGuestUser(): Boolean
  suspend fun saveIsGuestUser(isGuest: Boolean)
  suspend fun isForceUpdateRequired(): Boolean
  suspend fun saveForceUpdateRequired(isForceUpdate: Boolean)
  suspend fun isUserBlocked(): Boolean
  suspend fun saveUserBlocked(userBlocked: Boolean)
  suspend fun saveAdid(adid: String?)
  suspend fun getAdid(): String?
  suspend fun logout()
  companion object {
    val gameIdKey = stringPreferencesKey("gameId")
    val serverIdKey = stringPreferencesKey("serverId")
    val serverNameKey = stringPreferencesKey("serverName")
    val characterIdKey = stringPreferencesKey("characterId")
    val timeRemaining = stringPreferencesKey("timeRemaining")
    val guestLoginStartTime = stringPreferencesKey("guestLoginStartTime")
    val isGuestUser = stringPreferencesKey("isGuestUser")
    val isUserBlocked = stringPreferencesKey("isUserBlocked")
    val isForceUpdateRequired = stringPreferencesKey("isForceUpdateRequired")
    val adidKey = stringPreferencesKey("adid")
  }
}
