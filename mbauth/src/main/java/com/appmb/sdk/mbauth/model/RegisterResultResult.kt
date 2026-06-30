package com.appmb.sdk.mbauth.model

import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.model.MbAuthResult

sealed interface RegisterResult : MbAuthResult {
  class Success(val data: MbAuthData) : RegisterResult
  class UnknownServerConfiguration(val data: MbAuthData) : RegisterResult
  open class Error(
    val code: Int? = null,
    val message: String? = null,
  ) : RegisterResult {

    companion object {
      fun from(authErrorCodeResponse: AuthErrorCodeResponse): Error {
        return Error(
          code = authErrorCodeResponse.code,
          message = authErrorCodeResponse.description
        )
      }

      fun from(error: NetworkError): Error = if (error is NetworkError.ApiError) {
        Error(
          code = error.errorBody.status,
          message = error.errorBody.message
        )
      } else {
        Error(
          code = AuthErrorCodeResponse.UnknownError.code,
          message = AuthErrorCodeResponse.UnknownError.description
        )
      }
//      fun getError(error: NetworkError): Error = if (error is MbSdkErrorResponse) {
//        Error(
//          code = error.status,
//          message = error.message
//        )
//      } else {
//        CommonError
//      }
    }
//
//    object CommonError : Error(
//      code = -1, message = "Đã có lỗi xảy ra"
//    )
//
//    object UnSupport : Error(
//      code = -2, message = "Hệ thống không hỗ trợ phương thức đăng ký này"
//    )
  }
}

