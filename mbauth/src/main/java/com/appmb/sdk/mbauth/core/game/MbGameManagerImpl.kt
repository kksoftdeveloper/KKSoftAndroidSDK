package com.appmb.sdk.mbauth.core.game

import com.appmb.sdk.mbauth.model.game.GameInfoResult
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.domain.game.MbGameRepository
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


internal class MbGameManagerImpl(
  private val mbGameRepository: MbGameRepository,
  private val sdkConfig: MbSdkConfig
) : MbGameManager {
  override suspend fun getGameInfo(): Flow<GameInfoResult> = flow {
    if (sdkConfig.getServerClientId() == null) {
      emit(GameInfoResult.Error(message =  AuthErrorCodeResponse.AppNotConfiguredGameServer.description, code = AuthErrorCodeResponse.AppNotConfiguredGameServer.code))
    } else {
      mbGameRepository.fetchGameInfo()
        .fold(
          ifLeft = { error ->
            emit(GameInfoResult.Error(message = error.toString(), code = AuthErrorCodeResponse.UnknownError.code))
          },
          ifRight = { response ->
            response?.game?.let { gameData ->
              emit(GameInfoResult.Success(gameData))
            } ?: kotlin.run {
              emit(
                GameInfoResult.Error(
                  message = AuthErrorCodeResponse.AppNotConfiguredGame.description,
                  code = AuthErrorCodeResponse.AppNotConfiguredGame.code
                )
              )
            }
          }
      )
    }
  }

  override suspend fun getGameId(): Int {
    return mbGameRepository.getGameId()
  }
}
