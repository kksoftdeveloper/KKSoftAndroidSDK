package com.appmb.sdk.mbauth.ui.server

import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.model.MbAuthData


sealed interface ServerOperationState {
  object Idle : ServerOperationState
  object Maintenance : ServerOperationState
  data class GetServerIdsSuccess(val servers: List<MbServer>) : ServerOperationState
  data class Error(val message: String) : ServerOperationState
  data class UpdateServerClientIdSuccess(val authData: MbAuthData, val characterId: String) : ServerOperationState
  data class UpdateServerClientIdUnauthenticated(val message: String) : ServerOperationState
}