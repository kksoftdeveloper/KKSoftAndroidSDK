package com.appmb.sdk.mbauth.model

import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthResult

sealed interface LogoutResult : MbAuthResult {
  object Success : LogoutResult

  data class Error(
    val status: Int? = null,
    val message: String? = null,
  ) : LogoutResult {
    companion object {
      fun from(networkError: NetworkError): Error {
        if (networkError is NetworkError.ApiError) {
          return Error(
            status = networkError.errorBody.status,
            message = networkError.errorBody.message
          )
        }
        return Error(
          status = AuthErrorCodeResponse.UnknownError.code,
          message = AuthErrorCodeResponse.UnknownError.description
        )
      }

      fun from(authErrorCode: AuthErrorCodeResponse): Error {
        return Error(
          status = authErrorCode.code,
          message = authErrorCode.description
        )
      }
    }
  }
}