package com.appmb.sdk.mbcore.ext

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError

fun <T> Either<NetworkError, ResponseWrapper<T>>.mapDataNull(): Either<NetworkError, T?> {
  return this.flatMap { responseWrapper ->
    if (responseWrapper.isSuccessResponse()) {
      Either.Right(responseWrapper.data)
    } else {
      NetworkError.UnknownServerError.left()
    }
  }
}