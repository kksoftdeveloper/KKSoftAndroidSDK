package com.appmb.sdk.mbcore.model

import com.appmb.sdk.mbcore.error.NetworkError

sealed interface LoginResult : MbAuthResult {
  class Success(val data: MbAuthData) : LoginResult
  class UnknownServerConfiguration(val data: MbAuthData) : LoginResult
  data class Error(
    val status: Int? = null,
    val message: String? = null,
  ) : LoginResult {

    companion object {
      fun from(error: NetworkError): Error {
        if (error is NetworkError.ApiError) {
          return Error(
            status = error.errorBody.status,
            message = error.errorBody.message
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
