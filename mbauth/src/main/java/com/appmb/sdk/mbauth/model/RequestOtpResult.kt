package com.appmb.sdk.mbauth.model

import com.appmb.sdk.mbauth.data.dto.response.RequestOtpData
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthResult
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse


sealed interface RequestOtpResult : MbAuthResult {
  data class Success(val data: RequestOtpData) : RequestOtpResult
  data class Error(val code: Int, val message: String? = null) : RequestOtpResult {

//    object InvalidPhoneNumber : Error(
//      code = -3,
//      message = "Số điện thoại không hợp lệ"
//    )
//
//    object CommonError : Error(
//      code = -1,
//      message = "Đã có lỗi xảy ra"
//    )

    companion object {

      fun from(authErrorCodeResponse: AuthErrorCodeResponse): RequestOtpResult = Error(
        code = authErrorCodeResponse.code,
        message = authErrorCodeResponse.description
      )

      fun from(error: NetworkError): RequestOtpResult = when (error) {
        is NetworkError.ApiError -> Error(
          code = error.errorBody.status ?: AuthErrorCodeResponse.UnknownError.code,
          message = error.errorBody.message
        )

        else -> Error(
          code = AuthErrorCodeResponse.UnknownError.code,
          message = AuthErrorCodeResponse.UnknownError.description // "Đã có lỗi xảy ra"
        )
      }
    }
  }
}