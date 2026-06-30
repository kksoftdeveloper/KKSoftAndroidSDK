package com.kksoft.sdk

import android.app.Activity
import android.content.Context
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbpayment.MbPayment
import com.appmb.sdk.mbtracking.GameTracking
import com.appmb.sdk.mbtracking.di.TrackingLoader
import com.appmb.sdk.mbtracking.model.Level
import com.appmb.sdk.mbtracking.model.OnlineTime
import com.appmb.sdk.mbtracking.model.VIPLevel

object KKSoftAndroidSdk {
  @JvmStatic
  fun init(context: Context, config: MbSdkConfig) {
    MbSdk.init(context) { config }
    MbAuth.init()
    MbPayment.init()
    TrackingLoader.loadOnce()
  }

  @JvmStatic
  fun startAuthentication(activity: Activity, requestCode: Int) {
    MbAuth.startAuthenticationForResult(activity, requestCode)
  }

  @JvmStatic
  fun startCheckingForceUpdate(activity: Activity, requestCode: Int) {
    MbAuth.startCheckingForceUpdateForResult(activity, requestCode)
  }

  @JvmStatic
  fun startTokenExpiration(activity: Activity, requestCode: Int) {
    MbAuth.startTokenExpirationForResult(activity, requestCode)
  }

  @JvmStatic
  fun startLogout(activity: Activity, requestCode: Int) {
    MbAuth.startLogoutForResult(activity, requestCode)
  }

  @JvmStatic
  fun startLinkAccount(activity: Activity, requestCode: Int) {
    MbAuth.startLinkAccountScreenForResult(activity, requestCode)
  }

  @JvmStatic
  fun startDeactivateAccount(activity: Activity, requestCode: Int) {
    MbAuth.deactivateAccountForResult(activity, requestCode)
  }

  @JvmStatic
  fun startChangeGameServer(activity: Activity, requestCode: Int) {
    MbAuth.changeGameServerForResult(activity, requestCode)
  }

  @JvmStatic
  fun startPayment(activity: Activity) {
    MbPayment.startPayment(activity)
  }

  @JvmStatic
  fun logPlayGame(
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    GameTracking.logPlayGame(gameUUID, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logTutorialCompletedS1(
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    GameTracking.logTutorialCompletedS1(gameUUID, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logLevelUp(
    level: Level,
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    GameTracking.logLevelUp(level, gameUUID, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logVIPLevel(
    vipLevel: VIPLevel,
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    GameTracking.logVIPLevel(vipLevel, gameUUID, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logOnlineTime(
    onlineTime: OnlineTime,
    gameUUID: String,
    characterId: String,
    characterName: String,
    level: Level,
    serverId: String,
    serverName: String
  ) {
    GameTracking.logOnlineTime(
      onlineTime,
      gameUUID,
      characterId,
      characterName,
      level,
      serverId,
      serverName
    )
  }
}
