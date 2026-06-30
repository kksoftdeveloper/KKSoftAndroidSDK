package com.appmb.sdk.mbcore.network.api.auth

import arrow.core.Either
import com.appmb.sdk.mbcore.BuildConfig
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.request.MbRefreshTokenRequest
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.network.catchKtor
import com.appmb.sdk.mbcore.network.json
import com.appmb.sdk.mbcore.network.parseJsonToMapGson
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KtorCoreAuthApiImpl(
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val mixpanel: AnalyticsProvider
) : MbCoreAuthApi {
//
//  override suspend fun refreshToken(request: MbRefreshTokenRequest): Either<NetworkError, ResponseWrapper<MbRefreshTokenResponse>> =
//    withContext(ioDispatcher) {
//      Either.catchKtor {
//        val http = MbSdk.getAuthNetwork().httpClient
//        try {
//          val response = http.post {
//            url(path = MbCoreAuthApi.REFRESH_TOKEN_PATH)
//            setBody(request)
//            contentType(ContentType.Application.Json)
//          }
//          val eventName = response.request.url.toString()
//          val responseText = response.bodyAsText()
//          mixpanel.trackMap(
//            eventName = eventName,
//            properties = mapOf(
//              "body" to request.toString(),
//              "header" to response.request.headers.entries()
//                .associate { it.key to it.value.joinToString() },
//              "response" to parseJsonToMapGson(responseText)
//            )
//          )
//          response
//        } catch (e: Exception) {
//          mixpanel.trackMap(
//            eventName = BuildConfig.AUTH_BASE_URL + MbCoreAuthApi.REFRESH_TOKEN_PATH,
//            properties = mapOf(
//              "body" to request.toString(),
//              "error" to e.message
//            )
//          )
//          throw e
//        }
//      }
//    }

  override suspend fun refreshToken(
    request: MbRefreshTokenRequest
  ): Either<NetworkError, ResponseWrapper<MbRefreshTokenResponse>> = withContext(ioDispatcher) {
    Either.catchKtor<ResponseWrapper<MbRefreshTokenResponse>> {
      val http = MbSdk.getAuthNetwork().httpClient
      val response = http.post {
        url(path = MbCoreAuthApi.REFRESH_TOKEN_PATH)
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      response // catchKtor handles parsing + error mapping
    }.also { result ->
      when (result) {
        is Either.Right -> {
          val responseWrapper = result.value

          mixpanel.trackMap(
            eventName = BuildConfig.AUTH_BASE_URL + MbCoreAuthApi.REFRESH_TOKEN_PATH,
            properties = mapOf(
              "body" to request.toString(),
              "response" to parseJsonToMapGson(
                json.encodeToString(
                  ResponseWrapper.serializer(MbRefreshTokenResponse.serializer()), responseWrapper
                )
              )
            )
          )
        }

        is Either.Left -> {
          val error = result.value
          error.log() // Leverage your improved NetworkError.log()
          mixpanel.trackMap(
            eventName = BuildConfig.AUTH_BASE_URL + MbCoreAuthApi.REFRESH_TOKEN_PATH,
            properties = mapOf(
              "body" to request.toString(),
              "error" to error.toString()
            )
          )
        }
      }
    }
  }
}