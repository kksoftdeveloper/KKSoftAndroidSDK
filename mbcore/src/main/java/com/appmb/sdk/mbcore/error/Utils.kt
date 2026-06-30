package com.appmb.sdk.mbcore.error

import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import com.appmb.sdk.mbcore.network.json
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.decodeFromString

fun Throwable.toNetworkError(responseBody: String? = null): NetworkError =
  when (this) {
    is ClientRequestException -> responseBody?.let { body ->
      runCatching {
        val parsedError = json.decodeFromString<MbSdkErrorResponse>(body)
        NetworkError.ApiError(response.status.value, parsedError)
      }.getOrElse { NetworkError.JsonConvertError(it) }
    } ?: NetworkError.KtorError(this)

    is ServerResponseException,
    is RedirectResponseException -> NetworkError.KtorError(this)

    is IOException -> NetworkError.ClientRequestError(this)
    is TimeoutCancellationException -> NetworkError.HttpRequestTimeoutError(this)
    else -> NetworkError.KtorError(this)
  }
