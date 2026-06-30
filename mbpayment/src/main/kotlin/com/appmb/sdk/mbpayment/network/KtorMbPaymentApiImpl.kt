package com.appmb.sdk.mbpayment.network

import android.content.BroadcastReceiver
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import arrow.core.Either
import arrow.core.left
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbcore.BuildConfig
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import com.appmb.sdk.mbcore.network.catchKtor
import com.appmb.sdk.mbcore.network.json
import com.appmb.sdk.mbcore.network.parseJsonToMapGson
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import com.appmb.sdk.mbpayment.data.dto.ProductListData
import com.appmb.sdk.mbpayment.data.dto.request.ValidateGamePackageRequest
import com.appmb.sdk.mbpayment.data.dto.request.VerifyGamePackagePurchaseRequest
import com.appmb.sdk.mbpayment.data.dto.response.ValidatePackageResponse
import com.appmb.sdk.mbpayment.data.dto.response.VerifyGamePackagePurchaseResponse
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import org.koin.core.Koin
import org.koin.dsl.koinApplication

class KtorMbPaymentApiImpl(
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val mixpanel: AnalyticsProvider
) : MbPaymentApi {

  override suspend fun getProductIds(
    gameId: String,
    serverId: String?,
    size: Int,
    page: Int
  ): Either<NetworkError, ResponseWrapper<ProductListData>> = withContext(ioDispatcher) {
    Either.catchKtor {
      val path = MbPaymentApi.PRODUCT_IDS_PATH
      
      suspend fun apiCall(token: String?): HttpResponse {
        return MbSdk.getPaymentNetwork().httpClient.get {
          url(path = path)
          parameter("platform", "android")
          parameter("gameId", gameId)
          parameter("serverId", serverId)
          parameter("size", size)
          parameter("page", page)
          headers.append("Authorization", "Bearer $token")
          contentType(ContentType.Application.Json)
        }
      }

      executeApiCall(
        apiCall = ::apiCall,
        path = path,
        requestData = mapOf(
          "gameId" to gameId,
          "size" to size,
          "page" to page
        )
      )
    }
  }

  override suspend fun validateGamePackage(
    request: ValidateGamePackageRequest,
  ): Either<NetworkError, ResponseWrapper<ValidatePackageResponse>> = withContext(ioDispatcher) {
    Either.catchKtor {
      val path = MbPaymentApi.VALIDATE_GAME_PACKAGE_PATH
      
      suspend fun apiCall(token: String?): HttpResponse {
        return MbSdk.getPaymentNetwork().httpClient.post {
          url(path = path)
          headers.append("Authorization", "Bearer $token")
          setBody(request)
          contentType(ContentType.Application.Json)
        }
      }

      executeApiCall(
        apiCall = ::apiCall,
        path = path,
        requestData = mapOf("body" to request.toString())
      )
    }
  }

  override suspend fun verifyGamePackagePurchase(
    request: VerifyGamePackagePurchaseRequest,
  ): Either<NetworkError, ResponseWrapper<VerifyGamePackagePurchaseResponse>> =
    withContext(ioDispatcher) {
      Either.catchKtor {
        val path = MbPaymentApi.VERIFY_GAME_PACKAGE_PURCHASE_PATH
        
        suspend fun apiCall(token: String?): HttpResponse {
          return MbSdk.getPaymentNetwork().httpClient.post {
            url(path = path)
            setBody(request)
            headers.append("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
          }
        }

        executeApiCall(
          apiCall = ::apiCall,
          path = path,
          requestData = mapOf("body" to request.toString())
        )
      }
    }

  private suspend fun executeApiCall(
    apiCall: suspend (String?) -> HttpResponse,
    path: String,
    requestData: Map<String, Any>
  ): HttpResponse {
    try {
      var currentSession = getSession().getSessionData().firstOrNull()
      var response = apiCall(currentSession?.accessToken.orEmpty())

      val httpSuccess = response.status == HttpStatusCode.OK || 
                       response.status == HttpStatusCode.Created || 
                       response.status == HttpStatusCode.Accepted
      
      if (!httpSuccess) {
        val responseWrapper = json.decodeFromString<MbSdkErrorResponse>(string = response.bodyAsText())
        if (isTokenExpired(responseWrapper)) {
          val refreshResult = MbSdk.getPaymentNetwork().performTokenRefreshExplicitly()
          if (refreshResult.isRight()) {
            currentSession = getSession().getSessionData().firstOrNull()
            response = apiCall(currentSession?.accessToken.orEmpty())
          } else {
            throw NetworkError.TokenExpiredError()
          }
        }
      }

      trackSuccessEvent(response, requestData)
      return response
    } catch (e: Exception) {
      trackErrorEvent(path, requestData, e.message.orEmpty())
      throw e
    }
  }

  private fun isTokenExpired(responseWrapper: MbSdkErrorResponse): Boolean {
    return responseWrapper.status == AuthErrorCodeResponse.TokenExpired.code ||
           (responseWrapper.status == AuthErrorCodeResponse.UnknownError.code && 
            responseWrapper.message.lowercase() == "Jwt token already expired".lowercase())
  }

  private suspend fun trackSuccessEvent(response: HttpResponse, requestData: Map<String, Any>) {
    val eventName = response.request.url.toString()
    val responseText = response.bodyAsText()
    
    val properties = mutableMapOf<String, Any>().apply {
      putAll(requestData)
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
}