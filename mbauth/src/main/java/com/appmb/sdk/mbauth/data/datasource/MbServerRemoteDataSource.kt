package com.appmb.sdk.mbauth.data.datasource

import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.request.MbGetListServersRequest
import com.appmb.sdk.mbauth.data.dto.response.GetGameUuidData
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbauth.network.MbAuthApi
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError

class MbServerRemoteDataSource(
  private val mbSdkAuthApi: MbAuthApi,
) : MbServerDataSource {
  override suspend fun getGameUuid(
    gameId: String,
    serverId: String?,
  ): Either<NetworkError, ResponseWrapper<GetGameUuidData>> {
    return mbSdkAuthApi.getGameUuid(gameId, serverId)
  }

  override suspend fun getListServerIds(
    gameId: String,
  ): Either<NetworkError, ResponseWrapper<List<MbServer>>> {
    return mbSdkAuthApi.getListServers(gameId)
  }
}