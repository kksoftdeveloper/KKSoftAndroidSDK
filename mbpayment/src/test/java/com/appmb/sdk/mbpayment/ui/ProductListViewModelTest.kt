package com.appmb.sdk.mbpayment.ui

//import org.mockito.kotlin.anyInt
//import org.mockito.kotlin.anyString

import android.app.Activity
import android.content.Context
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import com.appmb.sdk.mbpayment.analytic.PaymentAnalytics
import com.appmb.sdk.mbpayment.data.dto.Pagination
import com.appmb.sdk.mbpayment.data.dto.ProductListData
import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus
import com.appmb.sdk.mbpayment.data.dto.ServerProduct
import com.appmb.sdk.mbpayment.data.dto.response.ValidatePackageResponse
import com.appmb.sdk.mbpayment.data.dto.response.VerifyGamePackagePurchaseResponse
import com.appmb.sdk.mbpayment.domain.BillingRepository
import com.appmb.sdk.mbpayment.domain.FetchListProductsUseCase
import com.appmb.sdk.mbpayment.domain.MbPaymentRepository
import com.appmb.sdk.mbpayment.model.GoogleBillingProduct
import com.appmb.sdk.mbpayment.testutils.MainDispatcherRule
import com.appmb.sdk.mbpayment.util.PlayServicesChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.coroutines.ContinuationInterceptor

/**
 * Unit tests for ProductListViewModel.
 *
 * Note: The init block of ProductListViewModel contains complex initialization logic
 * that depends on Android Context and GoogleApiAvailability. For these tests,
 * we focus on testing the public methods (onAction) and their side effects.
 */
@RunWith(MockitoJUnitRunner::class)
class ProductListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var fetchListProductsUseCase: FetchListProductsUseCase

    @Mock
    private lateinit var billingRepository: BillingRepository

    @Mock
    private lateinit var mbPaymentRepository: MbPaymentRepository

    @Mock
    private lateinit var mixpanel: AnalyticsProvider

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPackageManager: android.content.pm.PackageManager

    @Mock
    private lateinit var mockActivity: Activity

    private lateinit var viewModel: ProductListViewModel

    private val purchaseStatusFlow = MutableStateFlow<PurchaseStatus>(PurchaseStatus.Idle)

    // Test-controlled scope for stateIn in ProductListViewModel (initialized per-test inside runTest helper)
    // Note: we will use the runTest's TestScope as the ViewModel stateFlowScope per-test

    /**
     * Wrapper for runTest that automatically cancels ViewModel scope at the end.
     * This prevents "uncompleted coroutines" errors from the ViewModel's infinite collectLatest.
     * 
     * The helper advances both the runTest's scheduler and the Main dispatcher scheduler
     * so that any pending tasks (including cancellations) are processed before the test finishes.
     */
    private fun runTestWithViewModelCleanup(testBody: suspend TestScope.() -> Unit) {
        runTest {
            // Use the TestScope directly as stateFlowScope so runTest can cancel all coroutines
            val stateFlowScope = this

            val fakePlayServicesChecker = object : PlayServicesChecker {
                override fun isGooglePlayServicesAvailable(context: Context): Boolean = true
            }

            viewModel = ProductListViewModel(
                fetchListProductsUseCase = fetchListProductsUseCase,
                billingRepository = billingRepository,
                mbPaymentRepository = mbPaymentRepository,
                mixpanel = mixpanel,
                appContext = mockContext,
                playServicesChecker = fakePlayServicesChecker,
                stateFlowScope = stateFlowScope,
                externalScope = stateFlowScope,
                autoStartBilling = false // Disable auto-start to prevent init from setting error states
            )

            // Start billing observers explicitly so collectLatest runs on test scheduler
            viewModel.startBillingObservers()
            
            // Wait for startBillingObservers to complete its initial operations
            advanceUntilIdle()
            mainDispatcherRule.testScheduler.advanceUntilIdle()
            
            // Clear mock invocations from startBillingObservers so tests can verify their own calls
            org.mockito.Mockito.clearInvocations(mbPaymentRepository, fetchListProductsUseCase)

            try {
                testBody()
                advanceUntilIdle()
                mainDispatcherRule.testScheduler.advanceUntilIdle()
            } finally {
                // Explicitly cancel all child coroutines to ensure they're cancelled before runTest checks
                // This is necessary because collectLatest creates infinite coroutines that need explicit cancellation
                val job = stateFlowScope.coroutineContext[Job]
                job?.cancelChildren()
                // Wait for cancellation to propagate - advance multiple times to ensure all cancellations complete
                advanceUntilIdle()
                mainDispatcherRule.testScheduler.advanceUntilIdle()
                // Advance one more time to ensure cancellation propagation completes
                advanceUntilIdle()
                mainDispatcherRule.testScheduler.advanceUntilIdle()
            }
         }
     }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // No manual Dispatchers.setMain here - MainDispatcherRule sets Dispatchers.Main

        // Setup Context mock - required for GoogleApiAvailability calls in ViewModel init
        whenever(mockContext.packageManager).thenReturn(mockPackageManager)
        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockContext.packageName).thenReturn("com.test.package")
        
        // Mock PackageManager.getApplicationInfo to return a non-null ApplicationInfo
        // Use a real ApplicationInfo instance so we can set metaData (Mockito cannot stub fields)
        val appInfo = android.content.pm.ApplicationInfo()
        val meta = android.os.Bundle()
        // Provide the required Google Play Services meta-data to avoid GooglePlayServicesMissingManifestValueException
        meta.putInt("com.google.android.gms.version", 1)
        appInfo.metaData = meta
        // Use doReturn(...) to avoid calling the real Android framework method during stubbing
        org.mockito.Mockito.doReturn(appInfo).`when`(mockPackageManager).getApplicationInfo(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyInt()
        )

        // Setup default mock behaviors
        whenever(billingRepository.observePurchasesStatus()).thenReturn(purchaseStatusFlow)
        whenever(billingRepository.isClientReady()).thenReturn(true)
    }

    @After
    fun tearDown() {
        reset(
            fetchListProductsUseCase,
            billingRepository,
            mbPaymentRepository,
            mixpanel,
            mockContext,
            mockPackageManager,
            mockActivity
        )
    }

    @Test
    fun `onAction with ResetPurchaseStatusState should reset state and send PurchasedIdle event`() = runTestWithViewModelCleanup {
        // Wait for ViewModel initialization to complete (init block sends initial PurchasedIdle event)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()
        
        // Check error state before reset (for debugging)
        val errorBeforeReset = viewModel.state.error
        
        // When: ResetPurchaseStatusState action is triggered
        viewModel.onAction(ProductListAction.ResetPurchaseStatusState)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: State should be reset to idle
        assertEquals(PurchaseStatus.Idle, viewModel.state.purchasedStatus)
        assertFalse(viewModel.state.isLoading)
        
        // ResetPurchaseStatusState should clear the error
        // Note: If error persists after reset, it may be set by async operations after the reset completes
        val errorAfterReset = viewModel.state.error
        assertNull(
            "Error should be cleared by ResetPurchaseStatusState. Error before reset: $errorBeforeReset, Error after reset: $errorAfterReset",
            errorAfterReset
        )

        // Verify billing repository reset is called
        verify(billingRepository).resetPurchaseStatus()

        // Verify PurchasedIdle event is sent
        // Use take(1) to limit collection so the flow completes and doesn't create an infinite coroutine
        viewModel.events.first { it is ProductListEvent.PurchasedIdle }
    }

    @Test
    fun `onAction with RefreshProducts should reset page and load products`() = runTestWithViewModelCleanup {
        // Given: Mock successful product list response
        val productListData = ProductListData(
            content = listOf(
                ServerProduct(id = 1, sku = "com.kk.5k", price = 10000.0),
                ServerProduct(id = 2, sku = "com.kk.10k", price = 20000.0)
            ),
            pagination = Pagination(page = 0, limit = 10, totalPages = 1, total = 2)
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(productListData.right())

        val mockProducts = listOf(
            GoogleBillingProduct(
                productId = "com.kk.5k",
                name = "Product 1",
                points = "5000",
                pointUnit = "coins",
                price = "10000",
                formattedPrice = "10,000 VND",
                currency = "VND"
            ),
            GoogleBillingProduct(
                productId = "com.kk.10k",
                name = "Product 2",
                points = "10000",
                pointUnit = "coins",
                price = "20000",
                formattedPrice = "20,000 VND",
                currency = "VND"
            )
        )
        whenever(fetchListProductsUseCase(any())).thenReturn(mockProducts)

        // When: RefreshProducts action is triggered
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: Page should be reset to 0, products should be loaded
        assertEquals(0, viewModel.state.currentPage)
        assertEquals(2, viewModel.state.products.size)
        assertFalse(viewModel.state.isRefreshing)
        assertNull(viewModel.state.error)

        // Verify repository calls
        verify(mbPaymentRepository).getProductIds(page = 0, size = 10)
        verify(fetchListProductsUseCase).invoke(any())
    }

    @Test
    fun `onAction with LoadMoreProducts should increment page and append products`() = runTestWithViewModelCleanup {
        // Given: First load products to get initial state with page 0
        // Load exactly pageSize (10) products so hasMoreProducts is true
        val initialProductListData = ProductListData(
            content = (1..10).map { ServerProduct(id = it, sku = "com.kk.${it}k", price = it * 10000.0) },
            pagination = Pagination(page = 0, limit = 10, totalPages = 2, total = 20)
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(initialProductListData.right())

        val existingProducts = (1..10).map {
            GoogleBillingProduct(
                productId = "com.kk.${it}k",
                name = "Product $it",
                points = "${it * 1000}",
                pointUnit = "coins",
                price = "${it * 10000}",
                formattedPrice = "${it * 10000} VND",
                currency = "VND"
            )
        }
        whenever(fetchListProductsUseCase(any())).thenReturn(existingProducts)

        // Load initial products first
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Mock successful response for page 1
        val productListData = ProductListData(
            content = listOf(
                ServerProduct(id = 11, sku = "com.kk.11k", price = 110000.0)
            ),
            pagination = Pagination(page = 1, limit = 10, totalPages = 2, total = 20)
        )
        whenever(mbPaymentRepository.getProductIds(page = 1, size = 10))
            .thenReturn(productListData.right())

        val newProducts = listOf(
            GoogleBillingProduct(
                productId = "com.kk.11k",
                name = "Product 11",
                points = "11000",
                pointUnit = "coins",
                price = "110000",
                formattedPrice = "110,000 VND",
                currency = "VND"
            )
        )
        whenever(fetchListProductsUseCase(listOf("com.kk.11k"))).thenReturn(newProducts)

        // When: LoadMoreProducts action is triggered
        viewModel.onAction(ProductListAction.LoadMoreProducts)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: Page should be incremented, products should be appended
        assertEquals(1, viewModel.state.currentPage)
        assertEquals(11, viewModel.state.products.size) // 10 initial + 1 from page 1
        assertFalse(viewModel.state.isLoadingMore)

        // Verify repository calls
        verify(mbPaymentRepository).getProductIds(page = 1, size = 10)
        verify(fetchListProductsUseCase, org.mockito.kotlin.atLeastOnce()).invoke(any())
    }

    // Note: Testing "already loading" scenario is difficult without being able to set state directly.
    // The implementation has a guard: `if (state.isLoadingMore || !state.hasMoreProducts) return`
    // but isLoadingMore is only set inside the coroutine, creating a potential race condition.
    // This behavior is better tested in integration tests.

    @Test
    fun `onAction with LoadMoreProducts should not load if no more products`() = runTestWithViewModelCleanup {
        // Given: Load products with fewer than pageSize to set hasMoreProducts = false
        val productListData = ProductListData(
            content = listOf(ServerProduct(id = 1, sku = "com.kk.5k", price = 10000.0)),
            pagination = Pagination(page = 0, limit = 10, totalPages = 1, total = 1)
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(productListData.right())

        val products = listOf(
            GoogleBillingProduct(
                productId = "com.kk.5k",
                name = "Product 1",
                points = "5000",
                pointUnit = "coins",
                price = "10000",
                formattedPrice = "10,000 VND",
                currency = "VND"
            )
        )
        whenever(fetchListProductsUseCase(any())).thenReturn(products)

        // Load initial products (will set hasMoreProducts = false since only 1 product < pageSize)
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Verify hasMoreProducts is false
        assertFalse(viewModel.state.hasMoreProducts)

        // When: LoadMoreProducts action is triggered
        viewModel.onAction(ProductListAction.LoadMoreProducts)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: Repository should not be called again
        verify(mbPaymentRepository).getProductIds(page = 0, size = 10)
        verify(mbPaymentRepository, never()).getProductIds(page = 1, size = 10)
    }

    @Test
    fun `onAction with BuyProduct should validate and launch purchase on success`() = runTestWithViewModelCleanup {
        // Given: Mock successful validation response
        val validateResponse = ValidatePackageResponse(
            sku = "com.kk.5k",
            status = "ACTIVE",
            price = "10000",
            point = "5000"
        )
        whenever(
            mbPaymentRepository.validateGamePackage(
                any()
            )
        ).thenReturn(validateResponse.right())

        val buyAction = ProductListAction.BuyProduct(
            activity = mockActivity,
            productId = "com.kk.5k",
            price = "10000"
        )

        // When: BuyProduct action is triggered
        viewModel.onAction(buyAction)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: State should be updated, purchase should be launched
        assertFalse(viewModel.state.isLoading)
        assertEquals(PurchaseStatus.Idle, viewModel.state.purchasedStatus)

        // Verify repository validation call
        verify(mbPaymentRepository).validateGamePackage(any())
        verify(billingRepository).launchPurchase(eq(mockActivity), eq("com.kk.5k"))

        // Verify analytics tracking
        verify(mixpanel).trackMap(
            eq(PaymentAnalytics.clickToBuyAProduct),
            eq(mapOf("data" to "com.kk.5k"))
        )

        // Verify PurchasedIdle event is sent
        // Use take(1) to limit collection so the flow completes and doesn't create an infinite coroutine
        viewModel.events.first { it is ProductListEvent.PurchasedIdle }
    }

    @Test
    fun `onAction with BuyProduct should handle inactive package error`() = runTestWithViewModelCleanup {
        // Given: Mock validation response with inactive status
        val validateResponse = ValidatePackageResponse(
            sku = "com.kk.5k",
            status = "INACTIVE",
            price = "10000",
            point = "5000"
        )
        whenever(
            mbPaymentRepository.validateGamePackage(any())
        ).thenReturn(validateResponse.right())

        val buyAction = ProductListAction.BuyProduct(
            activity = mockActivity,
            productId = "com.kk.5k",
            price = "10000"
        )

        // When: BuyProduct action is triggered
        viewModel.onAction(buyAction)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Wait for the unavailable event to ensure the coroutine finished updating state
        viewModel.events.first { it is ProductListEvent.PurchasedUnavailableInSelectedServer }
        
        // Advance scheduler to ensure all state updates are processed
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: Should set error state and send unavailable event
        assertFalse(viewModel.state.isLoading)
        assertEquals(
            AuthErrorCodeResponse.DeactivatedOrNotFound.code,
            viewModel.state.error
        )
        assertEquals(
            PurchaseStatus.ProductUnavailableInGameServer,
            viewModel.state.purchasedStatus
        )

        // Verify purchase is not launched
        verify(billingRepository, never()).launchPurchase(any(), any())
    }

    @Test
    fun `onAction with BuyProduct should handle validation ApiError with DeactivatedOrNotFound`() = runTestWithViewModelCleanup {
        // Given: Mock validation error with DeactivatedOrNotFound status
        val apiError = NetworkError.ApiError(
            statusCode = 404,
            errorBody = MbSdkErrorResponse(
                status = AuthErrorCodeResponse.DeactivatedOrNotFound.code,
                code = AuthErrorCodeResponse.DeactivatedOrNotFound.code,
                message = "Package not found"
            )
        )
        whenever(
            mbPaymentRepository.validateGamePackage(any())
        ).thenReturn(apiError.left())

        val buyAction = ProductListAction.BuyProduct(
            activity = mockActivity,
            productId = "com.kk.5k",
            price = "10000"
        )

        // When: BuyProduct action is triggered
        viewModel.onAction(buyAction)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Wait for the unavailable event to ensure the coroutine finished updating state
        viewModel.events.first { it is ProductListEvent.PurchasedUnavailableInSelectedServer }
        
        // Advance scheduler to ensure all state updates are processed
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: Should set error state
        assertFalse(viewModel.state.isLoading)
        assertEquals(
            AuthErrorCodeResponse.DeactivatedOrNotFound.code,
            viewModel.state.error
        )
        assertEquals(
            PurchaseStatus.ProductUnavailableInGameServer,
            viewModel.state.purchasedStatus
        )

        // Verify purchase is not launched
        verify(billingRepository, never()).launchPurchase(any(), any())
    }

    @Test
    fun `onAction with BuyProduct should handle validation ApiError with other status`() = runTestWithViewModelCleanup {
        // Given: Mock validation error with other status
        val apiError = NetworkError.ApiError(
            statusCode = 500,
            errorBody = MbSdkErrorResponse(
                status = 500,
                code = 500,
                message = "Internal Server Error"
            )
        )
        whenever(
            mbPaymentRepository.validateGamePackage(any())
        ).thenReturn(apiError.left())

        val buyAction = ProductListAction.BuyProduct(
            activity = mockActivity,
            productId = "com.kk.5k",
            price = "10000"
        )

        // When: BuyProduct action is triggered
        viewModel.onAction(buyAction)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Wait for the error event to ensure the coroutine finished updating state
        viewModel.events.first { it is ProductListEvent.Error }
        
        // Advance scheduler to ensure all state updates are processed
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: Should set error state
        assertFalse(viewModel.state.isLoading)
        assertEquals(500, viewModel.state.error)
        assertEquals(PurchaseStatus.Error, viewModel.state.purchasedStatus)

        // Verify purchase is not launched
        verify(billingRepository, never()).launchPurchase(any(), any())
    }

    @Test
    fun `onAction with BuyProduct should handle non-ApiError`() = runTestWithViewModelCleanup {
        // Given: Mock non-ApiError (e.g., KtorError)
        val networkError = NetworkError.KtorError(Throwable("Network failure"))
        whenever(
            mbPaymentRepository.validateGamePackage(any())
        ).thenReturn(networkError.left())

        val buyAction = ProductListAction.BuyProduct(
            activity = mockActivity,
            productId = "com.kk.5k",
            price = "10000"
        )

        // When: BuyProduct action is triggered
        viewModel.onAction(buyAction)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Wait for the error event to ensure the coroutine finished updating state
        viewModel.events.first { it is ProductListEvent.Error }
        
        // Advance scheduler to ensure all state updates are processed
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: Should set error state with UnknownError code
        assertFalse(viewModel.state.isLoading)
        assertEquals(
            AuthErrorCodeResponse.UnknownError.code,
            viewModel.state.error
        )
        assertEquals(PurchaseStatus.Error, viewModel.state.purchasedStatus)

        // Verify purchase is not launched
        verify(billingRepository, never()).launchPurchase(any(), any())
    }

    @Test
    fun `loadProducts should handle TokenExpired error`() = runTestWithViewModelCleanup {
        // Given: Mock TokenExpired error
        val tokenExpiredError = NetworkError.KtorError(
            NetworkError.TokenExpiredError(/*Throwable("Token expired")*/)
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(tokenExpiredError.left())

        // When: RefreshProducts is triggered (which calls loadProducts)
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()

        // Then: TokenExpired event should be sent
        viewModel.events.first { it is ProductListEvent.TokenExpired }
    }

    @Test
    fun `loadProducts should use default product IDs when response is empty`() = runTestWithViewModelCleanup {
        // Given: Mock response with empty content
        val emptyProductListData = ProductListData(
            content = emptyList(),
            pagination = Pagination(page = 0, limit = 10, totalPages = 0, total = 0)
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(emptyProductListData.right())

        val defaultProducts = listOf(
            GoogleBillingProduct(
                productId = "com.kk.5k",
                name = "Default Product",
                points = "5000",
                pointUnit = "coins",
                price = "10000",
                formattedPrice = "10,000 VND",
                currency = "VND"
            )
        )
        whenever(fetchListProductsUseCase(any())).thenReturn(defaultProducts)

        // When: RefreshProducts is triggered
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()

        // Then: Default products should be loaded
        assertEquals(1, viewModel.state.products.size)
        // no event expected here, just assert products size
    }

    @Test
    fun `loadProducts should handle ApiError`() = runTestWithViewModelCleanup {
        // Given: Mock ApiError
        val apiError = NetworkError.ApiError(
            statusCode = 500,
            errorBody = MbSdkErrorResponse(
                status = 500,
                code = 500,
                message = "Internal Server Error"
            )
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(apiError.left())

        // When: RefreshProducts is triggered
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()

        // Then: Error should be set in state
        assertEquals(500, viewModel.state.error)
        assertEquals(0, viewModel.state.currentPage) // Should reset to 0 on error with reset=true
    }

    @Test
    fun `verifyProductPurchase should handle success`() = runTestWithViewModelCleanup {
        // Given: Mock successful verification response
        val verifyResponse = VerifyGamePackagePurchaseResponse(
            transactionCode = "TXN123",
            point = "5000"
        )
        whenever(
            mbPaymentRepository.verifyGamePackagePurchase(any())
        ).thenReturn(verifyResponse.right())

        // Trigger purchase status success to invoke verifyProductPurchase
        purchaseStatusFlow.value = PurchaseStatus.Success(
            productName = "Product 1",
            sku = "com.kk.5k",
            orderId = "ORDER123",
            purchaseToken = "TOKEN123"
        )
        advanceUntilIdle()

        // Then: State should be updated with success status
        assertTrue(viewModel.state.purchasedStatus is PurchaseStatus.Success)
        assertFalse(viewModel.state.isLoading)

        // Verify repository verification call
        verify(mbPaymentRepository).verifyGamePackagePurchase(any())

        // Verify success event is sent - use first() directly to collect until we find the matching event
        viewModel.events.first { it is ProductListEvent.PurchasedSuccess }
    }

    @Test
    fun `verifyProductPurchase should handle error`() = runTestWithViewModelCleanup {
        // Given: Mock verification error
        val apiError = NetworkError.ApiError(
            statusCode = 500,
            errorBody = MbSdkErrorResponse(
                status = 500,
                code = 500,
                message = "Verification failed"
            )
        )
        whenever(
            mbPaymentRepository.verifyGamePackagePurchase(any())
        ).thenReturn(apiError.left())

        // Trigger purchase status success to invoke verifyProductPurchase
        purchaseStatusFlow.value = PurchaseStatus.Success(
            productName = "Product 1",
            sku = "com.kk.5k",
            orderId = "ORDER123",
            purchaseToken = "TOKEN123"
        )
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: State should be updated with error status
        assertEquals(PurchaseStatus.Error, viewModel.state.purchasedStatus)
        assertEquals(500, viewModel.state.error)
        assertFalse(viewModel.state.isLoading)

        // Verify error event is sent - use first() directly to collect until we find the matching event
        // This will skip any earlier events (like PurchasedIdle) and wait for PurchasedError
        viewModel.events.first { it is ProductListEvent.PurchasedError }
    }

    @Test
    fun `purchase status flow should update state on UserCancelled`() = runTestWithViewModelCleanup {
        // Given: Purchase status flow emits UserCancelled
        purchaseStatusFlow.value = PurchaseStatus.UserCancelled
        advanceUntilIdle()

        // Then: State should be updated
        assertEquals(PurchaseStatus.UserCancelled, viewModel.state.purchasedStatus)

        // Verify event is sent
        viewModel.events.first { it is ProductListEvent.PurchasedUserCancel }
    }

    @Test
    fun `purchase status flow should update state on BillingUnavailable`() = runTestWithViewModelCleanup {
        // Given: Purchase status flow emits BillingUnavailable
        purchaseStatusFlow.value = PurchaseStatus.BillingUnavailable
        advanceUntilIdle()

        // Then: State should be updated
        assertEquals(PurchaseStatus.BillingUnavailable, viewModel.state.purchasedStatus)

        // Verify event is sent
        viewModel.events.first { it is ProductListEvent.PurchasedUnavailableBilling }
    }

    @Test
    fun `purchase status flow should update state on ProductUnavailable`() = runTestWithViewModelCleanup {
        // Given: Purchase status flow emits ProductUnavailable
        purchaseStatusFlow.value = PurchaseStatus.ProductUnavailable
        advanceUntilIdle()

        // Then: State should be updated
        assertEquals(PurchaseStatus.ProductUnavailable, viewModel.state.purchasedStatus)

        // Verify event is sent
        viewModel.events.first { it is ProductListEvent.PurchasedUnavailableProduct }
    }

    @Test
    fun `purchase status flow should update state on Error`() = runTestWithViewModelCleanup {
        // Given: Purchase status flow emits Error
        purchaseStatusFlow.value = PurchaseStatus.Error
        advanceUntilIdle()

        // Then: State should be updated
        assertEquals(PurchaseStatus.Error, viewModel.state.purchasedStatus)
        assertEquals(
            AuthErrorCodeResponse.UnknownError.code,
            viewModel.state.error
        )

        // Verify event is sent
        viewModel.events.first { it is ProductListEvent.PurchasedError }
    }

    @Test
    fun `loadProducts should set hasMoreProducts correctly when products size equals pageSize`() = runTestWithViewModelCleanup {
        // Given: Mock response with exactly pageSize products
        val productListData = ProductListData(
            content = (1..10).map { ServerProduct(id = it, sku = "com.kk.$it", price = it * 10000.0) },
            pagination = Pagination(page = 0, limit = 10, totalPages = 2, total = 20)
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(productListData.right())

        val products = (1..10).map {
            GoogleBillingProduct(
                productId = "com.kk.$it",
                name = "Product $it",
                points = "${it * 1000}",
                pointUnit = "coins",
                price = "${it * 10000}",
                formattedPrice = "${it * 10000} VND",
                currency = "VND"
            )
        }
        whenever(fetchListProductsUseCase(any())).thenReturn(products)

        // When: RefreshProducts is triggered
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: hasMoreProducts should be true (products.size == pageSize)
        assertTrue(viewModel.state.hasMoreProducts)
    }

    @Test
    fun `loadProducts should set hasMoreProducts to false when products size less than pageSize`() = runTestWithViewModelCleanup {
        // Given: Mock response with fewer than pageSize products
        val productListData = ProductListData(
            content = (1..5).map { ServerProduct(id = it, sku = "com.kk.$it", price = it * 10000.0) },
            pagination = Pagination(page = 0, limit = 10, totalPages = 1, total = 5)
        )
        whenever(mbPaymentRepository.getProductIds(page = 0, size = 10))
            .thenReturn(productListData.right())

        val products = (1..5).map {
            GoogleBillingProduct(
                productId = "com.kk.$it",
                name = "Product $it",
                points = "${it * 1000}",
                pointUnit = "coins",
                price = "${it * 10000}",
                formattedPrice = "${it * 10000} VND",
                currency = "VND"
            )
        }
        whenever(fetchListProductsUseCase(any())).thenReturn(products)

        // When: RefreshProducts is triggered
        viewModel.onAction(ProductListAction.RefreshProducts)
        advanceUntilIdle()
        mainDispatcherRule.testScheduler.advanceUntilIdle()

        // Then: hasMoreProducts should be false (products.size < pageSize)
        assertFalse(viewModel.state.hasMoreProducts)
    }
}
