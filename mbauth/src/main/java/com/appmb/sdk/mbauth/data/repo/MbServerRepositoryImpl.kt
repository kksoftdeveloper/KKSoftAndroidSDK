package com.appmb.sdk.mbauth.data.repo

import android.util.Log
import arrow.core.Either
import com.appmb.sdk.mbauth.data.datasource.MbCommonDataSource
import com.appmb.sdk.mbauth.data.datasource.MbServerDataSource
import com.appmb.sdk.mbauth.data.dto.response.GetGameUuidData
import com.appmb.sdk.mbauth.domain.MbServerRepository
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.data.datasource.game.MbGameDataSource
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.ext.mapDataNull

class MbServerRepositoryImpl(
  private val mbServerDataSource: MbServerDataSource,
  private val mbCommonDataSource: MbCommonDataSource
) : MbServerRepository {

  override suspend fun getGameUuid(serverId: String?): Either<NetworkError, GetGameUuidData?> {
    val response = mbServerDataSource.getGameUuid(
      gameId = mbCommonDataSource.getGameId(),
      serverId = serverId,
    ).mapDataNull()
    
    when (response) {
      is Either.Right -> {
        response.value?.let { data ->
          data.characterId?.let { characterId ->
            mbCommonDataSource.saveCharacterId(characterId)
          }
        }
      }
      is Either.Left -> {
        // Error case - no data to save
        Log.d(":::TAG", "Failed to get game UUID: ${response.value}")
      }
    }

    return response
  }

  override suspend fun getListServerIds(): Either<NetworkError, List<MbServer>?> {
    val response = mbServerDataSource.getListServerIds(
      gameId = mbCommonDataSource.getGameId(),
    ).mapDataNull()
    return response
  }
}