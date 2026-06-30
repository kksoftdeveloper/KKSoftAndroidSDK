package com.appmb.sdk.mbcore.data.dto.response

import android.annotation.SuppressLint
import com.appmb.sdk.mbcore.model.game.GameInfo
import com.appmb.sdk.mbcore.model.game.GameStatus
import kotlinx.serialization.Serializable

@Serializable
data class GameInfoResponse(
  val platform: String? = null,
  val game: GameInfoData? = null,
  val facebook: FacebookData? = null,
  val google: GoogleData? = null,
  val guestLoginAfterSeconds: Long? = null,
  val versionInfo: VersionInfo? = null,
)


@Serializable
data class GameInfoData(
  val gameId: Int? = null,
  val gameName: String? = null,
  val status: String? = null,

  ) {
  fun toEntity(): GameInfo {
    return GameInfo(
      gameId = gameId.toString().orEmpty(),
      gameName = gameName.orEmpty(),
      status = GameStatus.fromVale(status) ?: GameStatus.UNAVAILABLE
    )
  }
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class FacebookData(
  val clientId: String? = null,
  val clientToken: String? = null,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GoogleData(
  val clientId: String? = null,
  val platformUrlSchema: String? = null,
  val firebase: FirebaseConfig? = null,
)

@Serializable
data class FirebaseConfig(
  val apiKey: String? = null,
  val applicationId: String? = null,
  val databaseUrl: String? = null,
  val gaTrackingId: String? = null,
  val storageBucket: String? = null,
  val projectId: String? = null,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class VersionInfo(
  val forceUpdate: Boolean? = null,
  val minSdkVersion: String? = null,
  val minAppVersion: String? = null,
  val latestSdkVersion: String? = null,
  val latestAppVersion: String? = null,
)