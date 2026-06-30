package com.appmb.sdk.mbtracking

import android.content.Context
import android.util.Log
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbtracking.model.Level
import com.appmb.sdk.mbtracking.model.OnlineTime
import com.appmb.sdk.mbtracking.model.VIPLevel
import org.koin.java.KoinJavaComponent
import kotlin.jvm.JvmStatic

/**
 * Object for tracking in-game events.
 * Provides methods to track game play, tutorial completion, level ups, VIP levels, and online time.
 */
object GameTracking {
  
  private const val TAG = "GameTracking"
  
  private val context: Context
    get() = MbSdk.getContext()
  
  private fun trackingManager(): TrackingSdk? =
    runCatching { KoinJavaComponent.getKoin().get<TrackingSdk>() }.getOrNull()
  
  /**
   * Tracks when a user plays the game.
   * 
   * @param gameUUID The game UUID
   * @param characterId The character ID
   * @param characterName The character name
   * @param serverId The server ID
   * @param serverName The server name
   */
  @JvmStatic
  fun logPlayGame(
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    val mobileCarrier = trackingManager()?.getMobileCarrier() ?: "unknown"
    val params = mapOf(
      "af_uid" to gameUUID,
      "af_character_id" to characterId,
      "af_character_name" to characterName,
      "af_server_id" to serverId,
      "af_server_name" to serverName,
      "af_mobile_carrier" to mobileCarrier
    )
    val firebaseParams = mapOf(
      "user_id" to gameUUID,
      "character" to characterId,
//      "character_name" to characterName,
//      "server_id" to serverId,
//      "server_name" to serverName
    )
    val adjustParams = mapOf(
      "adj_uid" to gameUUID,
      "adj_character_id" to characterId,
      "adj_character_name" to characterName,
      "adj_server_id" to serverId,
      "adj_server_name" to serverName,
      "adj_mobile_carrier" to mobileCarrier
    )
    Log.d(TAG, "[TRACK] ${TrackingEvents.AF_PLAY_GAME} | gameUUID: $gameUUID | characterId: $characterId | serverId: $serverId | carrier: $mobileCarrier")
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_PLAY_GAME,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_play_game",
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_play_game",
            properties = adjustParams
          )
        )
      )
    )
  }
  
  /**
   * Tracks when a user completes tutorial S1.
   * 
   * @param gameUUID The game UUID
   * @param characterId The character ID
   * @param characterName The character name
   * @param serverId The server ID
   * @param serverName The server name
   */
  @JvmStatic
  fun logTutorialCompletedS1(
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    val mobileCarrier = trackingManager()?.getMobileCarrier() ?: "unknown"
    val params = mapOf(
      "af_uid" to gameUUID,
      "af_character_id" to characterId,
      "af_character_name" to characterName,
      "af_server_id" to serverId,
      "af_server_name" to serverName,
      "af_mobile_carrier" to mobileCarrier
    )
    val firebaseParams = mapOf(
      "user_id" to gameUUID,
      "character" to characterId,
//      "character_name" to characterName,
//      "server_id" to serverId,
//      "server_name" to serverName
    )
    val adjustParams = mapOf(
      "adj_uid" to gameUUID,
      "adj_character_id" to characterId,
      "adj_character_name" to characterName,
      "adj_server_id" to serverId,
      "adj_server_name" to serverName,
      "adj_mobile_carrier" to mobileCarrier
    )
    Log.d(TAG, "[TRACK] ${TrackingEvents.AF_TUTORIAL_COMPLETED_S1} | gameUUID: $gameUUID | characterId: $characterId | serverId: $serverId | carrier: $mobileCarrier")
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_TUTORIAL_COMPLETED_S1,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_tutorial_completed_s1",
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_tutorial_completed_s1",
            properties = adjustParams
          )
        )
      )
    )
  }
  
  /**
   * Tracks when a user levels up.
   * 
   * @param level The level reached
   * @param gameUUID The game UUID
   * @param characterId The character ID
   * @param characterName The character name
   * @param serverId The server ID
   * @param serverName The server name
   */
  @JvmStatic
  fun logLevelUp(
    level: Level,
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    val mobileCarrier = trackingManager()?.getMobileCarrier() ?: "unknown"
    val levelName = level.value // e.g., "level_50"
    val afEventName = "af_lev_$levelName" // e.g., "af_lev_level_50"
    val fbEventName = "fb_lev_$levelName" // e.g., "fb_lev_level_50"
    val adjEventName = "adj_lev_$levelName" // e.g., "adj_lev_level_50"
    
    val params = mapOf(
//      "af_level" to level.value,
//      "af_level_name" to level.toString(),
      "af_uid" to gameUUID,
      "af_character_id" to characterId,
      "af_character_name" to characterName,
      "af_server_id" to serverId,
      "af_server_name" to serverName,
      "af_mobile_carrier" to mobileCarrier
    )
    val firebaseParams = mapOf(
//      "level" to level.value,
//      "level_name" to level.toString(),
      "user_id" to gameUUID,
      "character" to characterId
//      "character_name" to characterName,
//      "server_id" to serverId,
//      "server_name" to serverName
    )
    val adjustParams = mapOf(
      "adj_uid" to gameUUID,
      "adj_character_id" to characterId,
      "adj_character_name" to characterName,
      "adj_server_id" to serverId,
      "adj_server_name" to serverName,
      "adj_mobile_carrier" to mobileCarrier
    )
    Log.d(TAG, "[TRACK] eventName: ${afEventName}, $fbEventName  | level: ${level.value} | gameUUID: $gameUUID | characterId: $characterId | serverId: $serverId | carrier: $mobileCarrier")
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = afEventName,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = fbEventName,
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = adjEventName,
            properties = adjustParams
          )
        )
      )
    )
  }
  
  /**
   * Tracks when a user reaches a VIP level.
   * 
   * @param level The VIP level reached
   * @param gameUUID The game UUID
   * @param characterId The character ID
   * @param characterName The character name
   * @param serverId The server ID
   * @param serverName The server name
   */
  @JvmStatic
  fun logVIPLevel(
    level: VIPLevel,
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
  ) {
    val mobileCarrier = trackingManager()?.getMobileCarrier() ?: "unknown"
    val levelName = level.value // e.g., "level_50"
    val afEventName = "af_vip_level_$levelName" // e.g., "af_vip_level_50"
    val fbEventName = "fb_vip_level_$levelName" // e.g., "fb_vip_level_10"
    val adjEventName = "adj_vip_level_$levelName" // e.g., "adj_vip_level_10"

    val params = mapOf(
//      "af_vip_level" to level.value,
//      "af_vip_level_name" to level.toString(),
      "af_uid" to gameUUID,
      "af_character_id" to characterId,
      "af_character_name" to characterName,
      "af_server_id" to serverId,
      "af_server_name" to serverName,
      "af_mobile_carrier" to mobileCarrier,
      "af_level" to level.value,
    )
    val firebaseParams = mapOf(
//      "vip_level" to level.value,
//      "vip_level_name" to level.toString(),
      "user_id" to gameUUID,
      "character" to characterId,
      "level" to level.value,
//      "character_name" to characterName,
//      "server_id" to serverId,
//      "server_name" to serverName
    )
    val adjustParams = mapOf(
      "adj_uid" to gameUUID,
      "adj_character_id" to characterId,
      "adj_character_name" to characterName,
      "adj_server_id" to serverId,
      "adj_server_name" to serverName,
      "adj_mobile_carrier" to mobileCarrier,
      "adj_level" to level.value,
    )
    Log.d(TAG, "[TRACK] eventName: ${afEventName}, $fbEventName | vipLevel: ${level.value} | gameUUID: $gameUUID | characterId: $characterId | serverId: $serverId | carrier: $mobileCarrier")
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = afEventName,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = fbEventName,
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = adjEventName,
            properties = adjustParams
          )
        )
      )
    )
  }
  
  /**
   * Tracks user's online time.
   * 
   * @param time The online time duration
   * @param gameUUID The game UUID
   * @param characterId The character ID
   * @param characterName The character name
   * @param level The current level
   * @param serverId The server ID
   * @param serverName The server name
   */
  @JvmStatic
  fun logOnlineTime(
    time: OnlineTime,
    gameUUID: String,
    characterId: String,
    characterName: String,
    level: Level,
    serverId: String,
    serverName: String
  ) {
    val mobileCarrier = trackingManager()?.getMobileCarrier() ?: "unknown"
    val levelName = time.minutes // e.g., "50"
    val afEventName = "af_online_${levelName}mins" // e.g., "af_online_30mins"
    val fbEventName = "fb_online_${levelName}mins" // e.g., "fb_online_30mins"
    val adjEventName = "adj_online_${levelName}mins" // e.g., "adj_online_30mins"

    val params = mapOf(
//      "af_online_time" to time.minutes,
//      "af_online_time_name" to time.toString(),
      "af_uid" to gameUUID,
      "af_character_id" to characterId,
      "af_character_name" to characterName,
      "af_level" to level.value,
//      "af_level_name" to level.toString(),
      "af_server_id" to serverId,
      "af_server_name" to serverName,
      "af_mobile_carrier" to mobileCarrier
    )
    val firebaseParams = mapOf(
//      "online_time" to time.minutes,
//      "online_time_name" to time.toString(),
      "user_id" to gameUUID,
      "character" to characterId,
//      "character_name" to characterName,
      "level" to level.value,
//      "level_name" to level.toString(),
//      "server_id" to serverId,
//      "server_name" to serverName
    )
    val adjustParams = mapOf(
      "adj_uid" to gameUUID,
      "adj_character_id" to characterId,
      "adj_character_name" to characterName,
      "adj_level" to level.value,
      "adj_server_id" to serverId,
      "adj_server_name" to serverName,
      "adj_mobile_carrier" to mobileCarrier
    )
    Log.d(TAG, "[TRACK] eventName: ${afEventName}, $fbEventName  | onlineTime: ${time.minutes}min | gameUUID: $gameUUID | characterId: $characterId | level: ${level.value} | serverId: $serverId")
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = afEventName,
          properties = params
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = fbEventName,
            properties = firebaseParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = adjEventName,
            properties = adjustParams
          )
        )
      )
    )
  }
}

