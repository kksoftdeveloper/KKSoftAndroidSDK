package com.appmb.sdk.mbcore.data.datasource.game

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.request.MbGameInfoRequest
import com.appmb.sdk.mbcore.data.dto.response.GameInfoResponse
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.error.NetworkError

interface MbGameDataSource {

  suspend fun getGameInfo(mbGameInfoRequest: MbGameInfoRequest): Either<NetworkError, ResponseWrapper<GameInfoResponse>>
  suspend fun getServerList(gameId: String): Either<NetworkError, ResponseWrapper<List<MbServer>>>
}