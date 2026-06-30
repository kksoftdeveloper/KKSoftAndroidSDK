package com.appmb.sdk.mbauth.model.game

import com.appmb.sdk.mbcore.data.dto.response.GameInfoData

data class GameInfo(
  val gameId: String,
  val gameName: String,
  val status: GameStatus,
)

sealed class GameInfoResult {
  class Success(val data: GameInfoData) : GameInfoResult()
  open class Error(
    val code: Int? = null,
    val message: String? = null,
  ) : GameInfoResult()
}

enum class GameStatus(val value: String?) {
  ACTIVE("ACTIVE"),
  UNAVAILABLE("UNAVAILABLE");

  companion object {
    fun fromVale(value: String?): GameStatus? =
      enumValues<GameStatus>().firstOrNull { it.value == value }
  }
}
