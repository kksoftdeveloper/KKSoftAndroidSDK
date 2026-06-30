package com.appmb.sdk.mbcore.error

import android.util.Log
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse

sealed interface NetworkError {
  fun log()

  data class ApiError(
    val statusCode: Int,
    val errorBody: MbSdkErrorResponse
  ) : NetworkError {
    override fun log() {
      Log.e("NetworkError", "ApiError (status=$statusCode): $errorBody")
    }
  }

  data class KtorError(val throwable: Throwable) : NetworkError {
    override fun log() {
      Log.e("NetworkError", "KtorError throwable: $throwable")
    }
  }

  object DataNullError : NetworkError {
    override fun log() {
      Log.e("NetworkError", "DataNullError")
    }
  }

  object UnknownServerError : NetworkError {
    override fun log() {
      Log.e("NetworkError", "UnknownServerError")
    }
  }

  data class JsonConvertError(val throwable: Throwable) : NetworkError {
    override fun log() {
      Log.e("NetworkError", "JsonConvertError throwable: $throwable")
    }
  }

  data class HttpRequestTimeoutError(val throwable: Throwable) : NetworkError {
    override fun log() {
      Log.e("NetworkError", "HttpRequestTimeoutError throwable: $throwable")
    }
  }

  data class ClientRequestError(val throwable: Throwable) : NetworkError {
    override fun log() {
      Log.e("NetworkError", "ClientRequestError throwable: $throwable")
    }
  }

  data class UnSupportOperationError(val message: String) : NetworkError {
    override fun log() {
      Log.e("NetworkError", "UnSupportOperationError message: $message")
    }
  }

  data class TokenExpiredError(
    val status: Int = -200,
    override val message: String = "Token has expired"
  ) : NetworkError, Throwable(message) {
    override fun log() {
      Log.e("NetworkError", "TokenExpiredError (status=$status): $message")
    }
  }

  data class UserBlockedError(
    val status: Int = -4004,
    override val message: String = "User is blocked"
  ) : NetworkError, Throwable(message) {
    override fun log() {
      Log.e("NetworkError", "TokenExpiredError (status=$status): $message")
    }
  }

  class NetworkException(message: String) {

  }
}
