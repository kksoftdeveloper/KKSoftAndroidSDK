package com.appmb.sdk.mbauth.core.server

import com.appmb.sdk.mbcore.model.server.GetListServerIdsResult
import com.appmb.sdk.mbauth.model.UpdateServerIdResult
import kotlinx.coroutines.flow.Flow


/**
 * Interface for managing server operations.
 */
internal interface MbServerManager {

  /**
   * Get list server ids for game client
   *
   * @return A Flow emitting a list of server ids.
   */
  suspend fun getListServerIds(): Flow<GetListServerIdsResult>

  /**
   * Update server id for game client
   *
   * @param serverId The new server id.
   * @return A Flow emitting the result of the update operation.
   */
  suspend fun getGameUuid(serverId: String): Flow<UpdateServerIdResult>

  /**
   * Update server client id
   * If unauthenticated, only saves serverName locally.
   * If authenticated and serverName is not null/empty, fetches server list, finds serverId by serverName,
   * saves serverId locally and calls characters/me API.
   *
   * @param serverName The server name to update.
   * @return A Flow emitting the result of the update operation.
   */
  suspend fun updateServerClientId(serverName: String?): Flow<UpdateServerIdResult>
}