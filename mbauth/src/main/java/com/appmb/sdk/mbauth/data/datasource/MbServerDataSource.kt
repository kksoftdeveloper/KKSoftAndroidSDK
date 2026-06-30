package com.appmb.sdk.mbauth.data.datasource

import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.request.MbGetListServersRequest
import com.appmb.sdk.mbauth.data.dto.response.GetGameUuidData
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError

/**
 * Interface for server data source.
 * Provides methods for get server ids and update server id operations.
 */
interface MbServerDataSource {

  /**
   * Update server id
   *
   * @param gameId The id of the game
   * @param serverId The new server id to update
   * @return A Flow emitting the result of updating server id
   */
  suspend fun getGameUuid(
    gameId: String,
    serverId: String?,
  ): Either<NetworkError, ResponseWrapper<GetGameUuidData>>

  suspend fun getListServerIds(
    gameId: String,
  ): Either<NetworkError, ResponseWrapper<List<MbServer>>>
}