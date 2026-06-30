package com.appmb.sdk.mbcore.network

import arrow.core.Either
import arrow.core.raise.either
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.error.toNetworkError
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.decodeFromString

suspend inline fun <reified T> Either.Companion.catchKtor(
  crossinline execute: suspend () -> HttpResponse,
): Either<NetworkError, T> = either {

  val response = catch {
    execute()
  }.mapLeft { throwable ->
    throwable.toNetworkError()
  }.bind()

  val responseText = catch { response.bodyAsText() }
    .mapLeft { NetworkError.JsonConvertError(throwable = it) }
    .bind()

  when (response.status.value) {
    in 200..299 -> {
      catch {
        json.decodeFromString<T>(string = responseText)
      }.mapLeft { NetworkError.JsonConvertError(throwable = it) }
        .bind()
    }

    in 400..499 -> {
      val apiError = catch {
        json.decodeFromString<MbSdkErrorResponse>(string = responseText)
      }.mapLeft { NetworkError.JsonConvertError(throwable = it) }
        .bind()

      shift(r = NetworkError.ApiError(statusCode = response.status.value, errorBody = apiError))
    }

    in 500..599 -> shift(
      r = NetworkError.KtorError(
        throwable = ServerResponseException(
          response,
          cachedResponseText = responseText
        )
      )
    )

    in 300..399 -> shift(
      r = NetworkError.KtorError(
        throwable = RedirectResponseException(
          response,
          cachedResponseText = responseText
        )
      )
    )

    else -> shift(
      r = NetworkError.KtorError(
        throwable = ResponseException(
          response,
          cachedResponseText = responseText
        )
      )
    )
  }
}