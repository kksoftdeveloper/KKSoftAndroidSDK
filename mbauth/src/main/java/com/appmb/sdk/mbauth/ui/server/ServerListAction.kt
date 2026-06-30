package com.appmb.sdk.mbauth.ui.server

sealed interface ServerListAction {

  object GetListServerIds : ServerListAction
  data class GetGameUuid(val serverId: String) : ServerListAction
  data class UpdateServerClientId(val serverId: String?) : ServerListAction
}