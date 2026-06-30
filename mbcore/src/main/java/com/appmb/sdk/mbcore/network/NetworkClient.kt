package com.appmb.sdk.mbcore.network

import android.util.Log
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.data.dto.request.MbRefreshTokenRequest
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import com.appmb.sdk.mbcore.utils.sha256
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NetworkClient(
  val baseUrl: String,
  val json: Json,
  private val mbCommonDataSource: MbCoreCommonDataSource,
  private val mbSdkConfig: MbSdkConfig
) {
  val httpClient = HttpClient(okHttpEngine) {
    install(ContentNegotiation) {
      json(json)
      register(
        ContentType.Text.Plain,
        KotlinxSerializationConverter(json)
      )
    }

    defaultRequest {
      url(baseUrl)
      contentType(ContentType.Application.Json)
    }

    install(Auth) {
      bearer {
        loadTokens {
          if (BearTokenProvider.accessToken?.isNotEmpty() == true && BearTokenProvider.refreshToken?.isNotEmpty() == true) {
            BearerTokens(
              accessToken = BearTokenProvider.accessToken!!,
              refreshToken = BearTokenProvider.refreshToken!!
            )
          } else {
            null
          }
        }
      }
    }

    install(Logging) {
      logger = object : Logger {
        override fun log(message: String) {
          Log.d("NetworkClient", message)
        }
      }
      level = LogLevel.ALL
    }

    install(HttpTimeout) {
      requestTimeoutMillis = 30_000  // Increased from 15s to 30s for slower networks
      connectTimeoutMillis = 60_000
      socketTimeoutMillis = 60_000
    }
  }

  suspend fun performTokenRefreshExplicitly(): Either<NetworkError, Unit> {
    val sessionData =
      getSession().getSessionData().firstOrNull() ?: return NetworkError.DataNullError.left()
    val refreshToken = sessionData.refreshToken
    val deviceId = mbCommonDataSource.getDeviceId()
    val deviceSecretId = mbCommonDataSource.getDeviceSecretId()

    val sign =
      "$deviceId|${mbCommonDataSource.getPlatform()}|${mbSdkConfig.getAuthSdkVersion()}|" +
          "${mbSdkConfig.getAppVersionName()}|$refreshToken|$deviceSecretId"

    val request = MbRefreshTokenRequest(
      deviceId = deviceId,
      platform = mbCommonDataSource.getPlatform(),
      sdkVersion = mbSdkConfig.getAuthSdkVersion(),
      appVersion = mbSdkConfig.getAppVersionName(),
      gameId = mbCommonDataSource.getGameId(),
      refreshToken = refreshToken,
      // Sign format - deviceId|platform|sdkVersion|appVersion|refreshToken|secretKey
      sign = sign.sha256(),
    )
    val httpClient = MbSdk.getAuthNetwork().httpClient
    httpClient.post {
      url("/sdk/api/v1/auth/refresh-token")
      contentType(ContentType.Application.Json)
      setBody(request)
    }.let { response ->
      if (response.status.isSuccess()) {
        val responseWrapper = json.decodeFromString<MbRefreshTokenResponse>(response.bodyAsText())
        BearTokenProvider.accessToken = responseWrapper.accessToken
        BearTokenProvider.refreshToken = responseWrapper.refreshToken
        if (responseWrapper.accessToken.isNullOrEmpty() || responseWrapper.refreshToken.isNullOrEmpty()) {
          Log.e("NetworkClient", "Received empty access or refresh token")
          return NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
              status = AuthErrorCodeResponse.Unauthorized.code,
              code = AuthErrorCodeResponse.Unauthorized.code,
              message = AuthErrorCodeResponse.Unauthorized.description
            )
          ).left()
        } else {
          Log.d("NetworkClient", "Token refreshed successfully")
          saveToken(
            accessToken = responseWrapper.accessToken,
            refreshToken = responseWrapper.refreshToken
          )
        }
        return Unit.right()
      } else {
        Log.e("NetworkClient", "Failed to refresh token: ${response.status}")
        return NetworkError.ApiError(
          statusCode = response.status.value,
          errorBody = MbSdkErrorResponse(
            status = AuthErrorCodeResponse.Unauthorized.code,
            code = AuthErrorCodeResponse.Unauthorized.code,
            message = AuthErrorCodeResponse.Unauthorized.description
          )
        ).left()
      }
    }
  }

  fun invalidateAuthTokens() {
    BearTokenProvider.accessToken = null
    BearTokenProvider.refreshToken = null
  }

  fun saveToken(accessToken: String, refreshToken: String) {
    BearTokenProvider.accessToken = accessToken
    BearTokenProvider.refreshToken = refreshToken
  }
}

class RetryRequestException(val originalRequest: HttpRequest) : Exception()


object BearTokenProvider {
  var accessToken: String? = null
  var refreshToken: String? = null
}