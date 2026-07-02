package com.appmb.sdk.mbcore.data.repo

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import arrow.core.Either
import arrow.core.left
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.data.datasource.game.MbGameDataSource
import com.appmb.sdk.mbcore.data.dto.request.MbGameInfoRequest
import com.appmb.sdk.mbcore.data.dto.request.toMap
import com.appmb.sdk.mbcore.data.dto.response.FirebaseConfig
import com.appmb.sdk.mbcore.data.dto.response.GameInfoResponse
import com.appmb.sdk.mbcore.domain.game.MbGameRepository
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.ext.mapDataNull
import com.appmb.sdk.mbcore.utils.VersionInfo
import com.appmb.sdk.mbcore.utils.sha256
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.koin.java.KoinJavaComponent
import kotlin.getValue

class MbGameRepositoryImpl(
  private val mbGameDataSource: MbGameDataSource,
  private val mbCommonDataSource: MbCoreCommonDataSource,
  private val mbSdkConfig: MbSdkConfig,
  private val mixpanel: AnalyticsProvider
) : MbGameRepository {

  companion object {
    @JvmStatic
    private var maintenanceNotified = false
  }

  val context: Context by KoinJavaComponent.inject(Context::class.java)

  override suspend fun fetchGameInfo(): Either<NetworkError, GameInfoResponse?> {
    val time = System.currentTimeMillis()
    val deviceId = mbCommonDataSource.getDeviceId()
    val deviceSecretKey = mbCommonDataSource.getDeviceSecretId()
    val appPackageName = mbSdkConfig.getAppId()
    val sdkVersion = if(mbSdkConfig.getAuthSdkVersion().isNullOrEmpty()) "1.0.0" else mbSdkConfig.getAuthSdkVersion()
    // deviceId|packageName|platform|appVersion|sdkVersion|timestamp|secretKey
    val keySign =
      "$deviceId|$appPackageName|${mbCommonDataSource.getPlatform()}|${mbSdkConfig.getAppVersionName()}" +
          "|${sdkVersion}|$time|$deviceSecretKey"

    Log.d("GameInfo-Sign", "deviceId|packageName|platform|appVersion|sdkVersion|timestamp|secretKey")
    Log.d("GameInfo-Sign", "${keySign}")

    val request = MbGameInfoRequest(
      packageName = appPackageName,
      deviceId = deviceId,
      platform = mbCommonDataSource.getPlatform(),
      appVersion = mbSdkConfig.getAppVersionName(),
      sdkVersion = sdkVersion,
      timestamp = time,
      sign = keySign.sha256()
    )
    mixpanel.trackMap(
      eventName = "getGameInfo",
      properties = request.toMap()
    )

    val response = mbGameDataSource.getGameInfo(request)
    when (response) {
      is Either.Right -> {
        val gameInfo = response.value
        gameInfo.data?.google?.let { gConfig ->
          gConfig.clientId?.let { clientId ->
            mbCommonDataSource.saveGoogleClientId(clientId)
          }
          gConfig.platformUrlSchema?.let {
            mbCommonDataSource.saveGooglePlatformUrlSchema(gConfig.platformUrlSchema)
          }
          gConfig.firebase?.let { firebaseConfig ->
            initializeFirebase(firebaseConfig)
          }
        }
        gameInfo.data?.facebook?.let { fConfig ->
          fConfig.clientId?.let { clientId ->
            mbCommonDataSource.saveFacebookAppId(clientId)
          }
          fConfig.clientToken?.let { clientToken ->
            mbCommonDataSource.saveFacebookClientToken(clientToken)
          }
        }
        VersionInfo.isForceUpdate = gameInfo.data?.versionInfo?.forceUpdate ?: false
        VersionInfo.minVersion = gameInfo.data?.versionInfo?.minAppVersion

        mbCommonDataSource.saveForceUpdateRequired(VersionInfo.isForceUpdate)

        gameInfo.data?.guestLoginAfterSeconds?.let {
          mbCommonDataSource.saveTimeRemaining(it.toString() ?: "300")
        }

        gameInfo.data?.game?.gameId?.let { gameId ->
          val normalizedGameId = gameId.takeIf { it >= 1 }?.toString() ?: "1"
          mbCommonDataSource.saveGameId(normalizedGameId)
          when (val serversEither = mbGameDataSource.getServerList(normalizedGameId)) {
            is Either.Right -> {
              serversEither.value.data?.let { serverList ->
                val server = serverList.findLast { it.serverName == mbSdkConfig.getServerClientId() }
                server?.serverId?.let { serverId ->
                  mbCommonDataSource.saveServerId(serverId)
                  mbSdkConfig.getServerClientId()?.let { mbCommonDataSource.saveServerName(it) }
                } /* ?: run {
                  if (!maintenanceNotified) {
                    maintenanceNotified = true
                    LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("ACTION_SERVER_MAINTENANCE"))
                  }
                } */
              }
            }
            is Either.Left -> {
              // log/track but DON’T fail fetchGameInfo
              // mixpanel.track("getServerList_failed", mapOf("code" to serversEither.value.code))
            }
          }
        }

        return response.mapDataNull()
      }

      is Either.Left -> {
        response.value.let {
          return it.left()
        }
      }
    }
  }

  override suspend fun getGameId(): Int {
    return mbCommonDataSource.getGameId().toIntOrNull()?.takeIf { it >= 1 } ?: 1
  }

  private fun initializeFirebase(config: FirebaseConfig) {
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        Log.i("FirebaseInit", "Firebase already initialized")
        return
      }

      val options = FirebaseOptions.Builder()
        .setApiKey(config.apiKey ?: "")
        .setApplicationId(config.applicationId ?: "")
        .setDatabaseUrl(config.databaseUrl)
        .setGaTrackingId(config.gaTrackingId)
        .setProjectId(config.projectId)
        .setStorageBucket(config.storageBucket)
        .build()

      FirebaseApp.initializeApp(context, options)
      Log.i("FirebaseInit", "Firebase initialized successfully with dynamic options")
    } catch (e: Exception) {
      Log.e("FirebaseInit", "Failed to initialize Firebase: ${e.message}")
    }
  }
}
