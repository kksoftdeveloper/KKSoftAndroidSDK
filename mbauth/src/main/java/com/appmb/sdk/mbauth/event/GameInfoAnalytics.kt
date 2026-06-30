package com.appmb.sdk.mbauth.event

interface GameInfoAnalytics {
  companion object {
    @JvmStatic
    val message: String
      get() = "message"

    @JvmStatic
    val request: String
      get() = "request"

    @JvmStatic
    val success: String
      get() = "success"

    @JvmStatic
    val failure: String
      get() = "failure"

    @JvmStatic
    val getGameServers: String
      get() = "getGameServers"

    @JvmStatic
    val getGameInfo: String
      get() = "getGameInfo"

    @JvmStatic
    val gameUUID: String
      get() = "gameUUID"

    @JvmStatic
    val characterId: String
      get() = "CharacterId"

    @JvmStatic
    val serverID: String
      get() = "serverID"

    @JvmStatic
    val updateGameServer: String
      get() = "updateGameServer"
  }
}