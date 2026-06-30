package com.appmb.sdk.mbauth.model

import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.model.MbAuthResult

sealed interface UpdateServerIdResult : MbAuthResult {
  data class Success(val authData: MbAuthData, val characterId: String) : UpdateServerIdResult
  data class Error(val code: Int, val message: String? = null) : UpdateServerIdResult {
    companion object {
      fun from(networkError: NetworkError): Error = if (networkError is NetworkError.ApiError) {
        Error(
          code = networkError.errorBody.status ?: AuthErrorCodeResponse.UnknownError.code,
          message = networkError.errorBody.message
        )
      } else {
        Error(
          code = AuthErrorCodeResponse.UnknownError.code,
          message = AuthErrorCodeResponse.UnknownError.description
        )
      }
    }
  }
}