package com.appmb.sdk.mbauth.model

import com.appmb.sdk.mbauth.data.dto.response.VerifyOtpData
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthResult
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse

sealed interface VerifyOtpResult : MbAuthResult {
  data class Success(val data: VerifyOtpData) : VerifyOtpResult
  data class Error(val code: Int, val message: String? = null) : VerifyOtpResult {

    companion object {

      fun from(authErrorCodeResponse: AuthErrorCodeResponse): Error = Error(
        code = authErrorCodeResponse.code,
        message = authErrorCodeResponse.description
      )

      fun from(error: NetworkError): Error = if (error is NetworkError.ApiError) {
        Error(
          code = error.errorBody.status ?: AuthErrorCodeResponse.UnknownError.code,
          message = error.errorBody.message
        )
      } else {
        Error(
          code = AuthErrorCodeResponse.UnknownError.code,
          message = AuthErrorCodeResponse.UnknownError.description // "Đã có lỗi xảy ra"
        )
      }
    }
  }
}