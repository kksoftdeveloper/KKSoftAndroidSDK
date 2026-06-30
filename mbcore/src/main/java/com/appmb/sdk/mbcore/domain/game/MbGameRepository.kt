package com.appmb.sdk.mbcore.domain.game

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.response.GameInfoResponse
import com.appmb.sdk.mbcore.error.NetworkError

interface MbGameRepository {
  suspend fun fetchGameInfo(): Either<NetworkError, GameInfoResponse?>
  suspend fun getGameId(): Int
}