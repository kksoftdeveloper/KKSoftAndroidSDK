package com.appmb.sdk.mbauth.model

import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.model.MbAuthResult

sealed interface GetSessionResult : MbAuthResult {
  data class Success(val data: MbAuthData) : GetSessionResult
  data class Error(
    val code: Int? = null,
    val message: String? = null,
  ) : GetSessionResult {

    companion object {
      fun from(authErrorCode: AuthErrorCodeResponse): Error {
        return Error(
          code = authErrorCode.code,
          message = if (authErrorCode.code == -400) "Session has not found" else authErrorCode.description
        )
      }

      fun from(error: NetworkError): Error {
        if (error is NetworkError.ApiError) {
          return Error(
            code = error.errorBody.status,
            message = error.errorBody.message
          )
        }
        return Error(
          code = AuthErrorCodeResponse.UnknownError.code,
          message = AuthErrorCodeResponse.UnknownError.description
        )
      }
    }
  }
}