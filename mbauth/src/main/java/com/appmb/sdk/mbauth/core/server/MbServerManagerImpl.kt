package com.appmb.sdk.mbauth.core.server

import android.util.Log
import com.appmb.sdk.mbauth.data.datasource.MbCommonDataSource
import com.appmb.sdk.mbauth.domain.MbServerRepository
import com.appmb.sdk.mbauth.event.GameInfoAnalytics
import com.appmb.sdk.mbcore.model.server.GetListServerIdsResult
import com.appmb.sdk.mbauth.model.UpdateServerIdResult
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the MbServerManager interface.
 */
internal class MbServerManagerImpl(
  private val repository: MbServerRepository,
  private val mixpanel: AnalyticsProvider,
  private val mbCommonDataSource: MbCommonDataSource
) : MbServerManager {

  override suspend fun getListServerIds(): Flow<GetListServerIdsResult> =
    flow {
      mixpanel.trackMap(
        eventName = GameInfoAnalytics.getGameServers,
        properties = mapOf(
          GameInfoAnalytics.success to "Getting game servers initialize"
        )
      )

      repository.getListServerIds()
        .fold(
          ifLeft = {
            mixpanel.trackMap(
              eventName = GameInfoAnalytics.getGameServers,
              properties = mapOf(
                GameInfoAnalytics.failure to if (it is NetworkError.ApiError) it.errorBody.message else "Unknown error"
              )
            )
            emit(GetListServerIdsResult.Error.from(it))
          },
          ifRight = {
            mixpanel.trackMap(
              eventName = GameInfoAnalytics.getGameServers,
              properties = mapOf(
                GameInfoAnalytics.success to "User gets game servers successfully"
              )
            )
            emit(GetListServerIdsResult.Success(it ?: emptyList()))
          }
        )
    }

  override suspend fun getGameUuid(serverId: String): Flow<UpdateServerIdResult> = flow {
    mixpanel.trackMap(
      eventName = GameInfoAnalytics.updateGameServer,
      properties = mapOf(GameInfoAnalytics.message to "Updating game server initializes")
    )
    repository.getGameUuid(serverId).fold(
      ifLeft = {
        mixpanel.trackMap(
          eventName = GameInfoAnalytics.updateGameServer,
          properties = mapOf(GameInfoAnalytics.failure to if (it is NetworkError.ApiError) it.errorBody.message else "Unknown error")
        )
        emit(UpdateServerIdResult.Error.from(it))
      },
      ifRight = { data ->
        mixpanel.trackMap(
          eventName = GameInfoAnalytics.updateGameServer,
          properties = mapOf(
            GameInfoAnalytics.success to "User updates game server successfully",
            GameInfoAnalytics.gameUUID to (data?.gameUuid ?: "Unknown game UUID"),
            GameInfoAnalytics.characterId to (data?.characterId ?: "Unknown characterId"),
            GameInfoAnalytics.serverID to serverId
          )
        )
        mbCommonDataSource.saveServerId(serverId)
        data?.let {
          val mbSession = MbAuthSessionFactory.getSession()
          val currentSession = mbSession.getSessionData().firstOrNull()
          currentSession?.let { session ->
            val newSession = session.copy(gameUuid = it.gameUuid, serverId = serverId)
            mbSession.save(newSession)
            emit(UpdateServerIdResult.Success(authData = newSession, characterId = it.characterId.orEmpty()))
          }
        }
      }
    )
  }

  override suspend fun updateServerClientId(serverName: String?): Flow<UpdateServerIdResult> = flow {
    val mbSession = MbAuthSessionFactory.getSession()
    val isAuthenticated = mbSession.isAuthenticated().firstOrNull() ?: false

    // Validate serverName
    if (serverName.isNullOrEmpty()) {
      mixpanel.trackMap(
        eventName = GameInfoAnalytics.updateGameServer,
        properties = mapOf(GameInfoAnalytics.failure to "Server name is null or empty")
      )
      emit(UpdateServerIdResult.Error(
        code = 400,
        message = "Server name cannot be null or empty"
      ))
      return@flow
    }

    // Fetch server list to find serverId by serverName
    mixpanel.trackMap(
      eventName = GameInfoAnalytics.updateGameServer,
      properties = mapOf(
        GameInfoAnalytics.message to "Fetching server list to find server ID",
        GameInfoAnalytics.serverID to serverName
      )
    )

    val serverListResult = repository.getListServerIds()
    val serverId = serverListResult.fold(
      ifLeft = {
        mixpanel.trackMap(
          eventName = GameInfoAnalytics.updateGameServer,
          properties = mapOf(
            GameInfoAnalytics.failure to "Failed to fetch server list: ${if (it is NetworkError.ApiError) it.errorBody.message else "Unknown error"}"
          )
        )
        emit(UpdateServerIdResult.Error.from(it))
        return@flow
      },
      ifRight = { serverList ->
        // Find server by serverName
        val matchedServer = serverList?.find { it.serverName == serverName }
        if (matchedServer == null || matchedServer.serverId.isNullOrEmpty()) {
          mixpanel.trackMap(
            eventName = GameInfoAnalytics.updateGameServer,
            properties = mapOf(
              GameInfoAnalytics.failure to "Server not found: $serverName"
            )
          )
          emit(UpdateServerIdResult.Error(
            code = 404,
            message = "Server with name '$serverName' not found"
          ))
          return@flow
        }
        matchedServer.serverId
      }
    )

    // Save serverId and serverName locally
    mbCommonDataSource.saveServerId(serverId)
    mbCommonDataSource.saveServerName(serverName)

    if (!isAuthenticated) {
      // User is not authenticated, serverId and serverName saved locally
      mixpanel.trackMap(
        eventName = GameInfoAnalytics.updateGameServer,
        properties = mapOf(
          GameInfoAnalytics.message to "Server ID and name saved locally (unauthenticated)",
          GameInfoAnalytics.serverID to serverId
        )
      )
      // Return error since we can't fetch the full data when unauthenticated
      emit(UpdateServerIdResult.Error(
        code = 401,
        message = "User is not authenticated. Server ID and name saved locally only."
      ))
      return@flow
    }

    // User is authenticated - Call the characters/me API
    mixpanel.trackMap(
      eventName = GameInfoAnalytics.updateGameServer,
      properties = mapOf(
        GameInfoAnalytics.message to "Updating server client ID with API call",
        GameInfoAnalytics.serverID to serverId
      )
    )

    repository.getGameUuid(serverId).fold(
      ifLeft = {
        mixpanel.trackMap(
          eventName = GameInfoAnalytics.updateGameServer,
          properties = mapOf(GameInfoAnalytics.failure to if (it is NetworkError.ApiError) it.errorBody.message else "Unknown error")
        )
        emit(UpdateServerIdResult.Error.from(it))
      },
      ifRight = { data ->
        data?.let {
          // Save gameUId and characterId (characterId is already saved in repository)
          val currentSession = mbSession.getSessionData().firstOrNull()
          currentSession?.let { session ->
            val newSession = session.copy(
              gameUuid = it.gameUuid,
              serverId = serverId
            )
            mbSession.save(newSession)
            
            mixpanel.trackMap(
              eventName = GameInfoAnalytics.updateGameServer,
              properties = mapOf(
                GameInfoAnalytics.success to "Server client ID updated successfully",
                GameInfoAnalytics.gameUUID to (it.gameUuid ?: "Unknown game UUID"),
                GameInfoAnalytics.characterId to (it.characterId ?: "Unknown characterId"),
                GameInfoAnalytics.serverID to serverId
              )
            )

            if (!it.characterId.isNullOrEmpty()) {
              mbCommonDataSource.saveCharacterId(it.characterId)
            }
            
            emit(UpdateServerIdResult.Success(authData = newSession, characterId = it.characterId.orEmpty()))
          } ?: run {
            emit(UpdateServerIdResult.Error(
              code = 500,
              message = "Failed to update session data"
            ))
          }
        } ?: run {
          emit(UpdateServerIdResult.Error(
            code = 500,
            message = "No data returned from API"
          ))
        }
      }
    )
  }
}