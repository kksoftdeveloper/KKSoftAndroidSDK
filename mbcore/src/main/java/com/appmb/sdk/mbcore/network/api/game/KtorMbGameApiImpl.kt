package com.appmb.sdk.mbcore.network.api.game

import arrow.core.Either
import com.appmb.sdk.mbcore.BuildConfig
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.request.MbGameInfoRequest
import com.appmb.sdk.mbcore.data.dto.response.GameInfoResponse
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.network.catchKtor
import com.appmb.sdk.mbcore.network.parseJsonToMapGson
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KtorMbGameApiImpl(
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val mixpanel: AnalyticsProvider
) : MbGameApi {

  override suspend fun getGameInfo(request: MbGameInfoRequest): Either<NetworkError, ResponseWrapper<GameInfoResponse>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        try {
          val response = http.post {
            url(path = MbGameApi.GAME_INFO_PATH)
            setBody(request)
            contentType(ContentType.Application.Json)
          }
          val eventName = response.request.url.toString()
          val responseText = response.bodyAsText()
          mixpanel.trackMap(
            eventName = eventName,
            properties = mapOf(
              "body" to request.toString(),
              "header" to response.request.headers.entries()
                .associate { it.key to it.value.joinToString() },
              "response" to parseJsonToMapGson(responseText)
            )
          )
          response
        } catch (e: Exception) {
          mixpanel.trackMap(
            eventName = BuildConfig.AUTH_BASE_URL + MbGameApi.GAME_INFO_PATH,
            properties = mapOf(
              "body" to request.toString(),
              "error" to e.message
            )
          )
          throw e
        }
      }
    }

  override suspend fun getListServerIds(gameId: String): Either<NetworkError, ResponseWrapper<List<MbServer>>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        try {
          val response = http.get {
            url(path = MbGameApi.GET_SERVER_GAME_PATH.replace("{gameId}", gameId))
            contentType(ContentType.Application.Json)
          }
          val eventName = response.request.url.toString()
          val responseText = response.bodyAsText()
          mixpanel.trackMap(
            eventName = eventName,
            properties = mapOf(
              "header" to response.request.headers.entries()
                .associate { it.key to it.value.joinToString() },
              "response" to parseJsonToMapGson(responseText)
            )
          )
          response
        } catch (e: Exception) {
          mixpanel.trackMap(
            eventName = BuildConfig.AUTH_BASE_URL + MbGameApi.GET_SERVER_GAME_PATH,
            properties = mapOf(
              "error" to e.message
            )
          )
          throw e
        }
      }
    }
}
