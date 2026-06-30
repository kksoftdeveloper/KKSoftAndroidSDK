package com.appmb.sdk.mbcore.model

import com.appmb.sdk.mbcore.error.NetworkError

sealed interface DeactivateAccountResult {
  object Success : DeactivateAccountResult
  data class Error(val code: Int, val message: String) : DeactivateAccountResult {
    companion object {
      fun from(error: NetworkError): Error {
        return if (error is NetworkError.ApiError) {
          Error(
            code = error.statusCode,
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
}