package com.appmb.sdk.mbcore.model.server

import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthResult

sealed interface GetListServerIdsResult : MbAuthResult {
  data class Success(val data: List<MbServer>) : GetListServerIdsResult
  data class Error(val code: Int, val message: String? = null) : GetListServerIdsResult {

    companion object {
      fun from(error: NetworkError): Error = if (error is NetworkError.ApiError) {
        Error(
          code = error.errorBody.status ?: AuthErrorCodeResponse.UnknownError.code,
          message = error.errorBody.message
        )
      } else {
        Error(
          code = AuthErrorCodeResponse.UnknownError.code,
          message = AuthErrorCodeResponse.UnknownError.description
        )
      }

      fun from(authErrorCodeResponse: AuthErrorCodeResponse): Error {
        return Error(
          code = authErrorCodeResponse.code,
          message = authErrorCodeResponse.description
        )
      }
    }
  }
}