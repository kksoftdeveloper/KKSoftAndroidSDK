package com.appmb.sdk.mbpayment.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbpayment.analytic.PaymentAnalytics
import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus
import com.appmb.sdk.mbpayment.data.dto.ServerProduct
import com.appmb.sdk.mbpayment.data.dto.toMixpanelEvent
import com.appmb.sdk.mbpayment.domain.BillingRepository
import com.appmb.sdk.mbpayment.domain.FetchListProductsUseCase
import com.appmb.sdk.mbpayment.domain.MbPaymentRepository
import com.appmb.sdk.mbpayment.model.GoogleBillingProduct
import com.appmb.sdk.mbpayment.model.MbPaymentParams
import com.google.android.gms.common.ConnectionResult
import com.appmb.sdk.mbpayment.tracking.PaymentTracking
import com.appmb.sdk.mbpayment.util.PlayServicesChecker
import com.appmb.sdk.mbpayment.util.DefaultPlayServicesChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductListViewModel(
  private val fetchListProductsUseCase: FetchListProductsUseCase,
  private val billingRepository: BillingRepository,
  private val mbPaymentRepository: MbPaymentRepository,
  private val mixpanel: AnalyticsProvider,
  private val appContext: Context,
  private val playServicesChecker: PlayServicesChecker = DefaultPlayServicesChecker(),
  // Inject a scope for stateIn so tests can control its lifecycle and scheduler
  private val stateFlowScope: kotlinx.coroutines.CoroutineScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO),
  // Optional external scope to run ViewModel coroutines (useful in tests). Defaults to viewModelScope.
  private val externalScope: kotlinx.coroutines.CoroutineScope? = null,
  // If false, the ViewModel will not start billing-related coroutines in init; tests can call startBillingObservers()
  private val autoStartBilling: Boolean = true,
) : ViewModel() {

  var state by mutableStateOf(ProductListState())
    private set

  // Store ServerProduct list with prices updated from GoogleBillingProduct
  private val _serverProducts = mutableListOf<ServerProduct>()
  val serverProducts: List<ServerProduct> get() = _serverProducts.toList()

  private val eventChannel = kotlinx.coroutines.channels.Channel<ProductListEvent>()
  val events = eventChannel.receiveAsFlow()

  private val _inAppBillingStatus: StateFlow<PurchaseStatus> =
    billingRepository
      .observePurchasesStatus()
      .stateIn(
        // use injected scope so tests can control coroutine lifecycle/scheduler
        stateFlowScope,
        SharingStarted.Lazily,
        PurchaseStatus.Idle
      )

  // Use externalScope when provided (tests will pass runTest's TestScope) so all coroutines
  // are structured under the test job and are cleaned up automatically.
  private val vmScope: kotlinx.coroutines.CoroutineScope = externalScope ?: viewModelScope

  init {
    vmScope.launch { eventChannel.send(ProductListEvent.PurchasedIdle) }

    // Auto-start billing observers unless disabled for tests
    if (autoStartBilling) {
      PaymentTracking.logIapStart()
      startBillingObservers()
    }

  }

  /**
   * Starts billing-related observers (Play Services check, billing connect, and collecting billing status).
   * Exposed so tests can control when these coroutines start to avoid leaking test schedulers.
   */
  fun startBillingObservers() {
    // Launch the entire billing observer flow on the injected scope so tests can control
    // its lifecycle (especially when using runTest's TestScope).
    stateFlowScope.launch {
      val isAvailable = try {
        playServicesChecker.isGooglePlayServicesAvailable(appContext)
      } catch (ex: Exception) {
        false
      }

      if (isAvailable) {
        try {
          billingRepository.connect()
          if (billingRepository.isClientReady()) {
            withContext(Dispatchers.Main) {
              state = state.copy(
                isGoogleBillingAvailable = true,
                isLoading = true,
                currentPage = 0,
                error = null
              )
            }
            loadProducts(reset = true)
            withContext(Dispatchers.Main) {
              state = state.copy(
                isLoading = false
              )
            }
//            mbPaymentRepository.getProductIds(
//              page = 0,
//              size = 10
//            ).fold(
//              ifLeft = {
//                Log.e("BillingViewModel", "getProductIds error: $it")
//                val errorCode: Int = (it as? NetworkError.ApiError)?.errorBody?.status
//                  ?: AuthErrorCodeResponse.UnknownError.code
////                state = state.copy(
////                  isLoading = false,
////                  error = errorCode
////                )
//
//                val productIds =
//                  listOf("com.kk.5k", "com.kk.10k", "com.kk.500k", "com.kk.20k", "com.kk.50k")
//                val productList = fetchListProductsUseCase(productIds)
//                state = state.copy(
//                  isLoading = false,
//                  products = productList
//                )
//              },
//              ifRight = { gamePackages ->
//                gamePackages ?: return@fold
//                var productIds = gamePackages.content.map { it.sku }
//                if (productIds.isEmpty()) productIds =
//                  listOf("com.kk.5k", "com.kk.10k", "com.kk.500k", "com.kk.20k", "com.kk.50k")
//                val productList = fetchListProductsUseCase(productIds)
//                state = state.copy(
//                  isLoading = false,
//                  products = productList
//                )
//              }
//            )
          }
        } catch (ex: Exception) {
          Log.e("BillingViewModel", "connect error: $ex")
          withContext(Dispatchers.Main) {
            state = state.copy(
              isLoading = false,
              error = AuthErrorCodeResponse.UnknownError.code
            )
          }
        }
      } else {
        withContext(Dispatchers.Main) {
          state = state.copy(
            isLoading = false,
            error = AuthErrorCodeResponse.GooglePlayServiceUnavailable.code
          )
        }
      }

      // Collect billing status on the same injected scope.
      _inAppBillingStatus.collectLatest {
        when (it) {
          PurchaseStatus.Error -> {
            val errorCode: Int = AuthErrorCodeResponse.UnknownError.code
            withContext(Dispatchers.Main) {
              state = state.copy(
                purchasedStatus = PurchaseStatus.Error,
                error = errorCode
              )
            }
            mixpanel.trackMap(
              eventName = PurchaseStatus.Error.toMixpanelEvent(),
              properties = emptyMap()
            )
            eventChannel.send(ProductListEvent.PurchasedError(errorCode))
          }

          is PurchaseStatus.Success -> {
            verifyProductPurchase(
              productName = it.productName,
              sku = it.sku,
              purchaseToken = it.purchaseToken,
              orderId = it.orderId.orEmpty()
            )
          }

          is PurchaseStatus.UserCancelled -> {
            withContext(Dispatchers.Main) {
              state = state.copy(
                purchasedStatus = PurchaseStatus.UserCancelled
              )
            }
            mixpanel.trackMap(
              eventName = PurchaseStatus.UserCancelled.toMixpanelEvent(),
              properties = emptyMap()
            )
            eventChannel.send(ProductListEvent.PurchasedUserCancel)
          }

          is PurchaseStatus.ProductUnavailableInGameServer -> {
            withContext(Dispatchers.Main) {
              state = state.copy(
                purchasedStatus = PurchaseStatus.ProductUnavailableInGameServer
              )
            }
            mixpanel.trackMap(
              eventName = PurchaseStatus.ProductUnavailableInGameServer.toMixpanelEvent(),
              properties = emptyMap()
            )
            eventChannel.send(ProductListEvent.PurchasedUnavailableInSelectedServer)
          }

          PurchaseStatus.Idle -> {
            withContext(Dispatchers.Main) {
              state = state.copy(
                purchasedStatus = PurchaseStatus.Idle
              )
            }
            eventChannel.send(ProductListEvent.PurchasedIdle)
          }

          PurchaseStatus.BillingUnavailable -> {
            withContext(Dispatchers.Main) {
              state = state.copy(
                purchasedStatus = PurchaseStatus.BillingUnavailable
              )
            }
            mixpanel.trackMap(
              eventName = PurchaseStatus.BillingUnavailable.toMixpanelEvent(),
              properties = emptyMap()
            )
            eventChannel.send(ProductListEvent.PurchasedUnavailableBilling)
          }

          PurchaseStatus.ProductUnavailable -> {
            withContext(Dispatchers.Main) {
              state = state.copy(
                purchasedStatus = PurchaseStatus.ProductUnavailable
              )
            }
            mixpanel.trackMap(
              eventName = PurchaseStatus.ProductUnavailable.toMixpanelEvent(),
              properties = emptyMap()
            )
            eventChannel.send(ProductListEvent.PurchasedUnavailableProduct)
          }
        }
      }
    }
  }

  fun onAction(action: ProductListAction) {
    when (action) {
      is ProductListAction.BuyProduct -> onBuyProduct(action)
      ProductListAction.ResetPurchaseStatusState -> handleResetPurchaseStatusState()
      is ProductListAction.RefreshProducts -> {
        onRefreshProducts()
      }

      is ProductListAction.LoadMoreProducts -> {
        onLoadMoreProducts()
      }
    }
  }

  private fun onRefreshProducts() {
    vmScope.launch {
      state = state.copy(
        isRefreshing = true,
        isLoading = false,
        currentPage = 0,
        error = null
      )
      loadProducts(reset = true)
      state = state.copy(
        isRefreshing = false,
        isLoading = false
      )
    }
  }

  private fun onLoadMoreProducts() {
    if (state.isLoadingMore || !state.hasMoreProducts) return
    val currentPage = state.currentPage
    state = state.copy(
      currentPage = currentPage + 1,
    )
    vmScope.launch {
      state = state.copy(isLoadingMore = true, error = null)
      loadProducts(reset = false)
      state = state.copy(isLoadingMore = false)
    }
  }

  private suspend fun loadProducts(reset: Boolean) {

    val response =
      mbPaymentRepository.getProductIds(page = state.currentPage, size = state.pageSize)
    response.fold(
      ifLeft = {
        if (it is NetworkError.KtorError) {
          if ((it.throwable is NetworkError.TokenExpiredError)) {
            eventChannel.send(ProductListEvent.TokenExpired)
          } else {
            val errorCode = (it as? NetworkError.ApiError)?.errorBody?.status
              ?: AuthErrorCodeResponse.UnknownError.code
            state = state.copy(error = errorCode)
          }
        } else {
          val errorCode = (it as? NetworkError.ApiError)?.errorBody?.status
            ?: AuthErrorCodeResponse.UnknownError.code
          state = state.copy(error = errorCode)
          if (reset) {
            state = state.copy(
              currentPage = 0
            )
          }
        }
      },
      ifRight = { gamePackages ->
        val serverProductsFromApi = gamePackages?.content ?: emptyList()
        val productIds = serverProductsFromApi.map { it.sku }
          .takeIf { it.isNotEmpty() }
          ?: listOf("com.kk.27kp", "com.kk.135kp", "com.kk.270kp", "com.kk.540kp", "com.kk.810kp", "com.kk.1080kp", "com.kk.2700kp")
        val googleBillingProducts = fetchListProductsUseCase(productIds)

        // Update GoogleBillingProduct prices with ServerProduct prices
        val updatedGoogleBillingProducts = updateGoogleBillingProductPrices(
          serverProducts = serverProductsFromApi,
          googleBillingProducts = googleBillingProducts
        )

        // Store ServerProduct list for reference
        if (reset) {
          _serverProducts.clear()
          _serverProducts.addAll(serverProductsFromApi)
        } else {
          _serverProducts.addAll(serverProductsFromApi)
        }

        state = if (reset) {
          state.copy(
            products = updatedGoogleBillingProducts,
            hasMoreProducts = updatedGoogleBillingProducts.size == state.pageSize // If you receive less than pageSize, end reached
          )
        } else {
          state.copy(
            products = state.products + updatedGoogleBillingProducts,
            hasMoreProducts = updatedGoogleBillingProducts.size == state.pageSize
          )
        }
      }
    )
  }

  private fun handleResetPurchaseStatusState() {
    billingRepository.resetPurchaseStatus()
    state = state.copy(
      isLoading = false,
      purchasedStatus = PurchaseStatus.Idle,
      error = null
    )
    vmScope.launch {
      eventChannel.send(ProductListEvent.PurchasedIdle)
    }
  }

  private fun onBuyProduct(intent: ProductListAction.BuyProduct) {
    mixpanel.trackMap(
      eventName = PaymentAnalytics.clickToBuyAProduct,
      properties = mapOf(
        "data" to intent.productId
      )
    )
    vmScope.launch {
      state = state.copy(
        isLoading = true
      )
      // Calling api validate product sku before launchPurchase
      mbPaymentRepository.validateGamePackage(
        MbPaymentParams.buildForValidateGamePackage(
          sku = intent.productId,
          price = intent.price,
        )
      ).fold(
        ifLeft = {
          val errorCode: Int = (it as? NetworkError.ApiError)?.errorBody?.status
            ?: AuthErrorCodeResponse.UnknownError.code
          PaymentTracking.logIapFailure(
            product = productInfoForSku(intent.productId),
            reason = "validation_failed",
            error = it
          )
          when (it) {
            is NetworkError.ApiError -> {
              when (it.errorBody.status) {
                AuthErrorCodeResponse.DeactivatedOrNotFound.code -> {
                  val purchaseStatus = PurchaseStatus.ProductUnavailableInGameServer
                  mixpanel.trackMap(
                    eventName = purchaseStatus.toMixpanelEvent(),
                    properties = mapOf(
                      "errorCode" to errorCode
                    )
                  )
                  eventChannel.send(ProductListEvent.PurchasedUnavailableInSelectedServer)
                  state = state.copy(
                    isLoading = false,
                    error = errorCode,
                    purchasedStatus = purchaseStatus
                  )
                }

                else -> {
                  val purchaseStatus = PurchaseStatus.Error
                  mixpanel.trackMap(
                    eventName = purchaseStatus.toMixpanelEvent(),
                    properties = mapOf(
                      "errorCode" to errorCode
                    )
                  )
                  eventChannel.send(ProductListEvent.Error(errorCode))
                  state = state.copy(
                    isLoading = false,
                    error = errorCode,
                    purchasedStatus = purchaseStatus
                  )
                }
              }
            }

            else -> {
              val purchaseStatus = PurchaseStatus.Error
              state = state.copy(
                isLoading = false,
                error = errorCode,
                purchasedStatus = purchaseStatus
              )
              mixpanel.trackMap(
                eventName = purchaseStatus.toMixpanelEvent(),
                properties = mapOf(
                  "errorCode" to errorCode
                )
              )
              eventChannel.send(ProductListEvent.Error(errorCode))
            }
          }
        },
        ifRight = {
          if (it?.status?.uppercase() == "ACTIVE") {
            state = state.copy(
              isLoading = false,
              purchasedStatus = PurchaseStatus.Idle
            )
            val result = billingRepository.launchPurchase(
              activity = intent.activity,
              productId = intent.productId
            )
            eventChannel.send(ProductListEvent.PurchasedIdle)
            Log.d("BillingViewModel", "---inside handleBuyProduct--- $result")
          } else {
            val errorCode = AuthErrorCodeResponse.DeactivatedOrNotFound.code
            state = state.copy(
              isLoading = false,
              error = errorCode,
              purchasedStatus = PurchaseStatus.ProductUnavailableInGameServer
            )
            PaymentTracking.logIapFailure(
              product = productInfoForSku(intent.productId),
              reason = "inactive_package",
              error = null
            )
            mixpanel.trackMap(
              eventName = PurchaseStatus.ProductUnavailableInGameServer.toMixpanelEvent(),
              properties = mapOf(
                "errorCode" to errorCode
              )
            )
            eventChannel.send(ProductListEvent.PurchasedUnavailableInSelectedServer)
          }
        }
      )
    }
  }

  private fun verifyProductPurchase(
    productName: String,
    sku: String,
    orderId: String,
    purchaseToken: String,
  ) {
    vmScope.launch {
      state = state.copy(
        isLoading = true
      )
      // Calling api to verify product purchase
      mbPaymentRepository.verifyGamePackagePurchase(
        MbPaymentParams.buildForVerifyPurchaseParams(
          sku = sku,
          orderId = orderId,
          purchaseToken = purchaseToken
        )
      ).fold(
        ifLeft = {
          val errorCode: Int = (it as? NetworkError.ApiError)?.errorBody?.status
            ?: AuthErrorCodeResponse.UnknownError.code
          val purchaseStatus = PurchaseStatus.Error
          state = state.copy(
            isLoading = false,
            error = errorCode,
            purchasedStatus = PurchaseStatus.Error
          )
          PaymentTracking.logIapFailure(
            product = productInfoForSku(sku),
            reason = "verification_failed",
            error = it,
            orderId = orderId
          )
          mixpanel.trackMap(
            eventName = purchaseStatus.toMixpanelEvent(),
            properties = mapOf(
              "errorCode" to errorCode
            )
          )
          eventChannel.send(ProductListEvent.PurchasedError(errorCode))
        },
        ifRight = {
          val purchaseStatus = PurchaseStatus.Success(
            productName = productName,
            sku = sku,
            purchaseToken = purchaseToken
          )
          state = state.copy(
            isLoading = false,
            purchasedStatus = purchaseStatus
          )
          PaymentTracking.logIapSuccess(
            product = productInfoForSku(sku),
            orderId = orderId
          )
          mixpanel.trackMap(
            eventName = purchaseStatus.toMixpanelEvent(),
            properties = mapOf(
              "productName" to productName,
              "sku" to sku,
              "orderId" to orderId,
              "purchaseToken" to purchaseToken
            )
          )
          eventChannel.send(
            ProductListEvent.PurchasedSuccess(
              productName = productName,
              sku = sku,
              orderId = orderId,
              purchaseToken = purchaseToken
            )
          )
        }
      )
    }
  }

  private fun productInfoForSku(sku: String): PaymentTracking.ProductInfo {
    val product = state.products.find { it.productId == sku }
    val price = product?.price?.toDoubleOrNull()
    return PaymentTracking.ProductInfo(
      productId = sku,
      price = price,
      currency = product?.currency
    )
  }

  /**
   * Updates GoogleBillingProduct prices with prices from ServerProduct.
   * Matches products by SKU and creates new GoogleBillingProduct instances with updated prices.
   */
  private fun updateGoogleBillingProductPrices(
    serverProducts: List<ServerProduct>,
    googleBillingProducts: List<GoogleBillingProduct>
  ): List<GoogleBillingProduct> {
    val serverProductMap = serverProducts.associateBy { it.sku }

    return googleBillingProducts.map { googleBillingProduct ->
      val serverProduct = serverProductMap[googleBillingProduct.productId]
      if (serverProduct != null) {
        // Update GoogleBillingProduct price with ServerProduct price
        // Convert Double to String for the price field
        val updatedPrice = serverProduct.price.toString()
        googleBillingProduct.copy(price = updatedPrice)
      } else {
        // If no matching ServerProduct found, keep original price from Google Play
        googleBillingProduct
      }
    }
  }

//  private fun productInfoForSku(sku: String): PaymentTracking.ProductInfo {
//    val product = state.products.find { it.productId == sku }
//    val price = product?.price?.toDoubleOrNull()
//    return PaymentTracking.ProductInfo(
//      productId = sku,
//      price = price,
//      currency = product?.currency
//    )
//  }

  /** Test helper: cancels the viewModelScope and injected stateFlowScope to stop any running coroutines. */
  fun cancelForTests() {
    try {
      // cancel ViewModel's scope
      viewModelScope.coroutineContext[Job]?.cancel()
    } catch (_: Throwable) {
    }

    try {
      // cancel injected stateFlowScope if it's cancelable
      stateFlowScope.coroutineContext[Job]?.cancel()
    } catch (_: Throwable) {
    }
    try {
      externalScope?.coroutineContext?.get(Job)?.cancel()
    } catch (_: Throwable) {
    }
  }
}
