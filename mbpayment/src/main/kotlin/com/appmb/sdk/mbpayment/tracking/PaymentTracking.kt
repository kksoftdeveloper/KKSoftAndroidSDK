package com.appmb.sdk.mbpayment.tracking

import android.content.Context
import android.util.Log
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbpayment.tracking.PaymentTracking.currentServerInfo
import com.appmb.sdk.mbtracking.TrackingEvent
import com.appmb.sdk.mbtracking.TrackingEventData
import com.appmb.sdk.mbtracking.TrackingEvents
import com.appmb.sdk.mbtracking.TrackingProviderType
import com.appmb.sdk.mbtracking.TrackingSdk
import com.appmb.sdk.mbtracking.util.CarrierUtils
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent

internal object PaymentTracking {

  data class ProductInfo(
    val productId: String,
    val price: Double? = null,
    val currency: String? = null
  )

  private data class ServerInfo(
    val characterId: String?,
    val serverId: String?,
    val serverName: String?
  )

  private const val TAG = "PaymentTracking"

  private val context: Context
    get() = MbSdk.getContext()

  private val commonDataSource: MbCoreCommonDataSource by lazy {
    KoinJavaComponent.getKoin().get()
  }

  private fun trackingManager(): TrackingSdk? =
    runCatching { KoinJavaComponent.getKoin().get<TrackingSdk>() }.getOrNull()

  private fun currentCarrier(): String =
    CarrierUtils.getCarrierName(context).ifBlank { "--" }

  private fun currentUid(): String? = MbSdk.getCurrentSessionData()?.gameUuid

  private fun currentServerInfo(): ServerInfo = runBlocking {
    val character = runCatching { commonDataSource.getCharacterId() }.getOrNull()
    val serverId = runCatching { commonDataSource.getServerId() }.getOrNull()
    val serverName = runCatching { commonDataSource.getServerName() }.getOrNull()
    ServerInfo(
      characterId = character?.takeIf { it.isNotBlank() },
      serverId = serverId?.takeIf { it.isNotBlank() },
      serverName = serverName?.takeIf { it.isNotBlank() },
    )
  }

  fun logIapStart() {
    runCatching {
    val uid = currentUid()
    val serverInfo = currentServerInfo()
    val carrier = currentCarrier()
    val afParams = mapOf(
      "af_uid" to uid,
      "af_character_id" to serverInfo.characterId,
      "af_server_id" to serverInfo.serverId,
      "af_server_name" to serverInfo.serverName,
      "af_mobile_carrier" to carrier
    )
    val fbParams = mapOf(
      "user_id" to (uid ?: ""),
      "character" to (serverInfo.characterId ?: ""),
//      "server_id" to (serverInfo.serverId ?: ""),
//      "server_name" to (serverInfo.serverName ?: ""),
//      "mobile_carrier" to carrier
    )
    val adjustParams = mapOf(
      "adj_uid" to uid,
      "adj_character_id" to serverInfo.characterId,
      "adj_server_id" to serverInfo.serverId,
      "adj_server_name" to serverInfo.serverName,
      "adj_mobile_carrier" to carrier
    )
    Log.d(TAG, "[PaymentTracking] Start IAP uid=${uid ?: "unknown"}")
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_START_IAP,
          properties = afParams
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_start_iap",
            properties = fbParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_start_iap",
            properties = adjustParams
          )
        )
      )
    )
    }.onFailure {
      Log.w(TAG, "[PaymentTracking] Skip start IAP tracking", it)
    }
  }

  fun logIapSuccess(product: ProductInfo, orderId: String) {
    runCatching {
    val uid = currentUid()
    val carrier = currentCarrier()
    val serverInfo = currentServerInfo()
    val afParams = mutableMapOf<String, Any?>(
      "af_package_id" to product.productId, //
      "af_order_id" to orderId, //
      "af_mobile_carrier" to carrier, //
      "af_character_id" to serverInfo.characterId, //
      "af_server_id" to serverInfo.serverId, //
      "af_server_name" to serverInfo.serverName //
    )
    product.price?.let { afParams["af_revenue"] = it } //
    product.currency?.let { afParams["af_currency"] = it } //
    uid?.let { afParams["af_uid"] = it } //

    val fbParams = mutableMapOf<String, Any?>(
      "item_name" to product.productId,
      "transaction_id" to orderId,
//      "mobile_carrier" to carrier,
      "character" to serverInfo.characterId, //
//      "server_id" to serverInfo.serverId,
//      "server_name" to serverInfo.serverName
    )
    product.price?.let { fbParams["value"] = it } //
    product.currency?.let { fbParams["currency"] = it } //
    uid?.let { fbParams["user_id"] = it } //

    val adjustParams = mutableMapOf<String, Any?>(
      "adj_package_id" to product.productId,
      "adj_order_id" to orderId,
      "adj_mobile_carrier" to carrier,
      "adj_character_id" to serverInfo.characterId,
      "adj_server_id" to serverInfo.serverId,
      "adj_server_name" to serverInfo.serverName
    )
    product.price?.let { adjustParams["adj_revenue"] = it }
    product.currency?.let { adjustParams["adj_currency"] = it }
    uid?.let { adjustParams["adj_uid"] = it }

    Log.d(TAG, "[PaymentTracking] IAP success sku=${product.productId} orderId=$orderId")
    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_PAY_SUCCESS,
          properties = afParams
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "in_app_purchase",
            properties = fbParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_pay_success",
            properties = adjustParams
          )
        )
      )
    )
    }.onFailure {
      Log.w(TAG, "[PaymentTracking] Skip IAP success tracking", it)
    }
  }

  fun logIapFailure(
    product: ProductInfo,
    reason: String,
    error: NetworkError?,
    orderId: String? = null
  ) {
    runCatching {
    val uid = currentUid()
    val carrier = currentCarrier()
    val serverInfo = currentServerInfo()
    val afParams = mutableMapOf<String, Any?>(
      "af_package_id" to product.productId,
      "af_order_status" to reason,
      "af_mobile_carrier" to carrier, //
      "af_character_id" to serverInfo.characterId, //
      "af_server_id" to serverInfo.serverId, //
      "af_server_name" to serverInfo.serverName //
    )
//    product.price?.let { afParams["af_revenue"] = it }
    product.currency?.let { afParams["af_currency"] = it }
//    orderId?.let { afParams["af_order_id"] = it }

    uid?.let { afParams["af_uid"] = it } //

//    var errorCode: Any? = null
//    var errorMessage: Any? = null
//    when (error) {
//      is NetworkError.ApiError -> {
//        errorCode = error.errorBody.status
//        errorMessage = error.errorBody.message
//      }
//
//      is NetworkError.KtorError -> {
//        errorCode = "ktor"
//        errorMessage = error.throwable?.message
//      }
//
//      is NetworkError.ClientRequestError -> {
//        errorCode = "client_request"
//        errorMessage = error.throwable?.message
//      }
//
//      is NetworkError.JsonConvertError -> {
//        errorCode = "json_convert"
//        errorMessage = error.throwable?.message
//      }
//
//      else -> {
//        errorMessage = "unknown error"
//      }
//    }
//    errorCode?.let { afParams["af_error_code"] = it }
//    errorMessage?.let { afParams["af_error_message"] = it }

    val fbParams = mutableMapOf<String, Any?>(
      "item_name" to product.productId,
//      "order_status" to reason,
      "character" to serverInfo.characterId, //
    )
    uid?.let { fbParams["user_id"] = it }
    product.currency?.let { fbParams["currency"] = it }

//    orderId?.let { fbParams["order_id"] = it }
//    product.price?.let { fbParams["revenue"] = it }


//    errorCode?.let { fbParams["error_code"] = it }
//    errorMessage?.let { fbParams["error_message"] = it }

//    Log.w(TAG, "[PaymentTracking] IAP failure sku=${product.productId} reason=$reason code=$errorCode")
    val adjustParams = mutableMapOf<String, Any?>(
      "adj_package_id" to product.productId,
      "adj_order_status" to reason,
      "adj_mobile_carrier" to carrier,
      "adj_character_id" to serverInfo.characterId,
      "adj_server_id" to serverInfo.serverId,
      "adj_server_name" to serverInfo.serverName
    )
    product.currency?.let { adjustParams["adj_currency"] = it }
    uid?.let { adjustParams["adj_uid"] = it }

    trackingManager()?.track(
      TrackingEvent(
        defaultData = TrackingEventData(
          name = TrackingEvents.AF_PAY_NOTYET_SUCCESS,
          properties = afParams
        ),
        overrides = mapOf(
          TrackingProviderType.FIREBASE to TrackingEventData(
            name = "fb_pay_notyet_success",
            properties = fbParams
          ),
          TrackingProviderType.ADJUST to TrackingEventData(
            name = "adj_pay_notyet_success",
            properties = adjustParams
          )
        )
      )
    )
    }.onFailure {
      Log.w(TAG, "[PaymentTracking] Skip IAP failure tracking", it)
    }
  }
}
