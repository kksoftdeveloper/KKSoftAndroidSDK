package com.appmb.sdk.mbauth.network.impl

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import arrow.core.Either
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbauth.data.dto.request.MbGetListServersRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLinkSocialAccountRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginByPhoneRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginGuestRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginSocialRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRegisterRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRequestOtpRequest
import com.appmb.sdk.mbauth.data.dto.request.MbResetPasswordRequest
import com.appmb.sdk.mbauth.data.dto.request.MbVerifyOtpRequest
import com.appmb.sdk.mbauth.data.dto.response.GetGameUuidData
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.data.dto.response.RequestOtpData
import com.appmb.sdk.mbauth.data.dto.response.ResetPasswordDataModel
import com.appmb.sdk.mbauth.data.dto.response.VerifyOtpData
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbauth.network.MbAuthApi
import com.appmb.sdk.mbcore.BuildConfig
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.di.IsolatedKoinContext
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import com.appmb.sdk.mbcore.network.api.game.MbGameApi
import com.appmb.sdk.mbcore.network.catchKtor
import com.appmb.sdk.mbcore.network.json
import com.appmb.sdk.mbcore.network.parseJsonToMapGson
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.inject
import org.koin.core.Koin
import org.koin.java.KoinJavaComponent.inject
import kotlin.toString

class KtorMbAuthApiImpl(
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val mixpanel: AnalyticsProvider
) : MbAuthApi {

  val context: Context by inject(Context::class.java)

  private suspend fun executeApiCall(
    apiCall: suspend (String?) -> HttpResponse,
    path: String,
    requestData: Map<String, Any>?,
    isPublicApi: Boolean = false
  ): HttpResponse {
    try {

      var currentSession = if(isPublicApi) null else getSession().getSessionData().firstOrNull()
      var response = apiCall(if(isPublicApi) null else currentSession?.accessToken.orEmpty())

      val httpSuccess = response.status == HttpStatusCode.OK ||
          response.status == HttpStatusCode.Created ||
          response.status == HttpStatusCode.Accepted

      val isUserBlocked = currentSession?.userBlocked == true
      if (isUserBlocked) {
        val resultIntent = Intent(MbAuth.ACTION_USER_BLOCKED)
        LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
        throw NetworkError.UserBlockedError()
      }
      if (!httpSuccess) {
        val responseWrapper = json.decodeFromString<MbSdkErrorResponse>(string = response.bodyAsText())
        if (isTokenExpired(responseWrapper)) {
          val refreshResult = MbSdk.getPaymentNetwork().performTokenRefreshExplicitly()
          if (refreshResult.isRight()) {
            currentSession = getSession().getSessionData().firstOrNull()
            response = apiCall(currentSession?.accessToken.orEmpty())
          } else {
            val resultIntent = Intent(MbAuth.ACTION_TOKEN_EXPIRATION)
            LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            throw NetworkError.TokenExpiredError()
          }
        }
      }

      trackSuccessEvent(response, requestData)
      return response
    } catch (e: Exception) {
      trackErrorEvent(path, requestData ?: emptyMap(), e.message.orEmpty())
      throw e
    }
  }

  private fun isTokenExpired(responseWrapper: MbSdkErrorResponse): Boolean {
    return responseWrapper.status == AuthErrorCodeResponse.TokenExpired.code ||
        (responseWrapper.status == AuthErrorCodeResponse.UnknownError.code &&
            responseWrapper.message.lowercase() == "Jwt token already expired".lowercase())
  }

  private suspend fun trackSuccessEvent(response: HttpResponse, requestData: Map<String, Any>?) {
    val eventName = response.request.url.toString()
    val responseText = response.bodyAsText()

    val properties = mutableMapOf<String, Any>().apply {
      putAll(requestData ?: emptyMap())
      put("header", response.request.headers.entries()
        .associate { it.key to it.value.joinToString() })
      put("response", parseJsonToMapGson(responseText))
    }

    mixpanel.trackMap(eventName = eventName, properties = properties)
  }

  private fun trackErrorEvent(path: String, requestData: Map<String, Any>, errorMessage: String) {
    val eventName = BuildConfig.PAYMENT_BASE_URL + path
    val properties = mutableMapOf<String, Any>().apply {
      putAll(requestData)
      put("error", errorMessage)
    }
    mixpanel.trackMap(eventName = eventName, properties = properties)
  }

  override suspend fun loginByPhone(request: MbLoginByPhoneRequest?): Either<NetworkError, ResponseWrapper<LoginDataModel>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.LOGIN_PATH

        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            setBody(request)
            contentType(ContentType.Application.Json)
            token?.let { headers.append("Authorization", "Bearer $it") }
          }
        }

        executeApiCall(
          apiCall = ::apiCall,
          path = path,
          requestData = mapOf("body" to request.toString()),
          isPublicApi = true
        )
      }
    }

  override suspend fun loginGuest(request: MbLoginGuestRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.LOGIN_PATH

        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            setBody(request)
            contentType(ContentType.Application.Json)
            token?.let { headers.append("Authorization", "Bearer $it") }
          }
        }

        executeApiCall(
          apiCall = ::apiCall,
          path = path,
          requestData = mapOf("body" to request.toString()),
          isPublicApi = true
        )
      }
    }

  override suspend fun loginBySocial(request: MbLoginSocialRequest?): Either<NetworkError, ResponseWrapper<LoginDataModel>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.LOGIN_PATH

        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            setBody(request)
            contentType(ContentType.Application.Json)
            token?.let { headers.append("Authorization", "Bearer $it") }
          }
        }

        executeApiCall(
          apiCall = ::apiCall,
          path = path,
          requestData = mapOf("body" to request.toString()),
          isPublicApi = true
        )
      }
    }

  override suspend fun register(request: MbRegisterRequest?): Either<NetworkError, ResponseWrapper<RegisterDataModel>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.REGISTER_PATH

        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            setBody(request)
            contentType(ContentType.Application.Json)
            token?.let { headers.append("Authorization", "Bearer $it") }
          }
        }
        executeApiCall(
          apiCall = ::apiCall,
          path = path,
          requestData = mapOf("body" to request.toString())
        )
      }
    }

  override suspend fun logout(): Either<NetworkError, Unit> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.LOGOUT_PATH

        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            headers.append("Authorization", "Bearer ${token.orEmpty()}")
            contentType(ContentType.Application.Json)
          }
        }
        val currentSession = getSession().getSessionData().firstOrNull()
        executeApiCall(
          apiCall = { apiCall(currentSession?.accessToken) },
          path = path,
          requestData = emptyMap()
        )
      }
    }

  override suspend fun linkSocialAccount(request: MbLinkSocialAccountRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.LINK_ACCOUNT_PATH
        val currentSession = getSession().getSessionData().firstOrNull()

        Log.i("LINK-ACCOUNT", "current-session = $currentSession")

        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            setBody(request)
            headers.append("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
          }
        }

        executeApiCall(
          apiCall = { apiCall(currentSession?.accessToken) },
          path = path,
          requestData = mapOf("body" to request.toString())
        )
      }
    }

  override suspend fun linkPhoneAccount(request: MbRegisterRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.LINK_ACCOUNT_PATH
        val currentSession = getSession().getSessionData().firstOrNull()

        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            setBody(request)
            headers.append("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
          }
        }

        executeApiCall(
          apiCall = { apiCall(currentSession?.accessToken) },
          path = path,
          requestData = mapOf("body" to request.toString())
        )

//        try {
//          val response = http.post {
//            url(path = MbAuthApi.LINK_ACCOUNT_PATH)
//            headers.append("Authorization", "Bearer ${currentSession?.accessToken.orEmpty()}")
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
//            eventName = BuildConfig.AUTH_BASE_URL + MbAuthApi.LINK_ACCOUNT_PATH,
//            properties = mapOf(
//              "body" to request.toString(),
//              "error" to e.message
//            )
//          )
//          throw e
//        }
      }
    }

  override suspend fun getGameUuid(
    gameId: String,
    serverId: String?,
  ): Either<NetworkError, ResponseWrapper<GetGameUuidData>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = if (serverId.isNullOrEmpty()) {
            "/sdk/api/v1/characters/me?gameId=$gameId"
        } else {
            "/sdk/api/v1/characters/me?gameId=$gameId&serverId=$serverId"
        }
        val currentSession = getSession().getSessionData().firstOrNull()

        suspend fun apiCall(token: String?): HttpResponse {
          return http.get {
            url(path = path)
            token?.let { tk ->
              if (tk.isNotEmpty()) {
                headers.append("Authorization", "Bearer $tk")
              }
            }
            contentType(ContentType.Application.Json)
          }
        }

        executeApiCall(
          apiCall = { apiCall(currentSession?.accessToken) },
          path = path,
          requestData = emptyMap()
        )
      }
    }

  override suspend fun requestOtp(request: MbRequestOtpRequest): Either<NetworkError, ResponseWrapper<RequestOtpData>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val path = MbAuthApi.REQUEST_OTP_PATH
        suspend fun apiCall(token: String?): HttpResponse {
          return http.post {
            url(path = path)
            setBody(request)
            contentType(ContentType.Application.Json)
            token?.let { headers.append("Authorization", "Bearer $it") }
          }
        }
        executeApiCall(
          apiCall = ::apiCall,
          path = path,
          requestData = mapOf("body" to request.toString())
        )
      }
    }

  override suspend fun verifyOtp(request: MbVerifyOtpRequest): Either<NetworkError, ResponseWrapper<VerifyOtpData>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        try {
          val response = http.post {
            url(path = MbAuthApi.VERIFY_OTP_PATH)
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
            eventName = BuildConfig.AUTH_BASE_URL + MbAuthApi.VERIFY_OTP_PATH,
            properties = mapOf(
              "body" to request.toString(),
              "error" to e.message
            )
          )
          throw e
        }
      }
    }

  override suspend fun resetPassword(request: MbResetPasswordRequest): Either<NetworkError, ResponseWrapper<ResetPasswordDataModel>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        try {
          val response = http.post {
            url(path = MbAuthApi.RESET_PASSWORD_PATH)
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
            eventName = BuildConfig.AUTH_BASE_URL + MbAuthApi.RESET_PASSWORD_PATH,
            properties = mapOf(
              "body" to request.toString(),
              "error" to e.message
            )
          )
          throw e
        }
      }
    }

  override suspend fun deactivateAccount(): Either<NetworkError, Unit> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        val currentSession = getSession().getSessionData().firstOrNull()
        try {
          val response = http.post {
            url(path = MbAuthApi.DEACTIVATE_ACCOUNT_PATH)
            headers.append("Authorization", "Bearer ${currentSession?.accessToken.orEmpty()}")
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
            eventName = BuildConfig.AUTH_BASE_URL + MbAuthApi.DEACTIVATE_ACCOUNT_PATH,
            properties = mapOf(
              "error" to e.message
            )
          )
          throw e
        }
      }
    }

  override suspend fun getListServers(gameId: String): Either<NetworkError, ResponseWrapper<List<MbServer>>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val http = MbSdk.getAuthNetwork().httpClient
        try {
          val response = http.get {
            url(path = MbAuthApi.GET_LIST_SERVERS_PATH.replace("{gameId}", gameId))
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
