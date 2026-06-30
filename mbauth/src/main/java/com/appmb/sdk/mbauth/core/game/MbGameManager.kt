package com.appmb.sdk.mbauth.core.game

import com.appmb.sdk.mbauth.model.game.GameInfoResult
import kotlinx.coroutines.flow.Flow

internal interface MbGameManager {
  suspend fun getGameInfo(): Flow<GameInfoResult>

  suspend fun getGameId(): Int
}