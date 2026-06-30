package com.appmb.sdk.mbauth.domain

import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.response.GetGameUuidData
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.error.NetworkError

/**
 * Interface for server repository.
 * Provides methods for get list server id, update server id operations.
 */
interface MbServerRepository {
  /**
   * Update server id and get game Uuid
   *
   * @param serverId The id of the server
   *
   * @return A Flow emitting the result of update server id operation.
   */
  suspend fun getGameUuid(serverId: String?): Either<NetworkError, GetGameUuidData?>
  suspend fun getListServerIds(): Either<NetworkError, List<MbServer>?>
}