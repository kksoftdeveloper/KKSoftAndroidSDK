package com.appmb.sdk.mbcore.network.api.game

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.request.MbGameInfoRequest
import com.appmb.sdk.mbcore.data.dto.response.GameInfoResponse
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.error.NetworkError

interface MbGameApi {

  suspend fun getGameInfo(request: MbGameInfoRequest): Either<NetworkError, ResponseWrapper<GameInfoResponse>>

  suspend fun getListServerIds(
    gameId: String
  ): Either<NetworkError, ResponseWrapper<List<MbServer>>>

  companion object {
    const val GAME_INFO_PATH = "sdk/api/v1/games/info"
    const val GET_SERVER_GAME_PATH = "sdk/api/v1/games/{gameId}/servers"
  }
}