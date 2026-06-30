package com.appmb.sdk.mbauth.model

import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthResult
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse


sealed interface ResetPasswordResult : MbAuthResult {
  object Success : ResetPasswordResult
  data class Error(val code: Int, val message: String? = null) : ResetPasswordResult {

    companion object {

      fun from(authErrorCodeResponse: AuthErrorCodeResponse): ResetPasswordResult =
        Error(
          code = authErrorCodeResponse.code,
          message = authErrorCodeResponse.description
        )

      fun from(error: NetworkError): ResetPasswordResult =
        if (error is NetworkError.ApiError) {
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
    }
  }
}