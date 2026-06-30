package com.appmb.sdk.mbauth.core.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.event.LogoutAnalytics
import com.appmb.sdk.mbauth.model.LogoutResult
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
import com.appmb.sdk.mbauth.worker.WorkerManager
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.domain.auth.MbCoreAuthRepository
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.DeactivateAccountResult
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import com.appmb.sdk.mbcore.session.MbAuthSession
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for MbAuthManagerImpl.logout() method.
 * 
 * Uses a testable wrapper class that accepts dependencies via constructor injection
 * to enable proper unit testing.
 */
@RunWith(MockitoJUnitRunner::class)
class MbAuthManagerImplLogoutTest {

    @Mock
    private lateinit var mbAuthProviderFactory: MbAuthProviderFactory

    @Mock
    private lateinit var mixpanel: AnalyticsProvider

    @Mock
    private lateinit var mbAuthRepository: MbAuthRepository

    @Mock
    private lateinit var mbCoreAuthRepository: MbCoreAuthRepository

    @Mock
    private lateinit var mbCoreCommonDataSource: MbCoreCommonDataSource

    @Mock
    private lateinit var mockSession: MbAuthSession

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun tearDown() {
        // Clean up if needed
    }

    @Test
    fun `logout should emit Success when repository logout succeeds`() = runTest {
        // Given: Repository returns success
        whenever(mbAuthRepository.logout()).thenReturn(Unit.right())

        // Note: To actually run this test, you would need to:
        // 1. Refactor MbAuthManagerImpl to accept dependencies via constructor, OR
        // 2. Use Koin test utilities to set up a test Koin instance
        
        // Expected behavior:
        // - Should emit LogoutResult.Success
        // - Should call WorkerManager.cancelTimer()
        // - Should call session.clear()
        // - Should call mbCoreCommonDataSource.logout()
        // - Should track analytics events
        
        // Example assertion (when testable):
         val manager = TestableMbAuthManagerImpl(
             mbAuthProviderFactory,
             mixpanel,
             mbAuthRepository,
             mbCoreAuthRepository,
             mbCoreCommonDataSource,
             mockSession
         )
         val result = manager.logout().first()
         assertTrue(result is LogoutResult.Success)
         verify(mbAuthRepository).logout()
         verify(mockSession).clear()
         verify(mbCoreCommonDataSource).logout()
    }

    @Test
    fun `logout should emit Error when repository logout fails with ApiError`() = runTest {
        // Given: Repository returns ApiError
        val apiError = NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
                status = 401,
                message = "Unauthorized"
            )
        )
        whenever(mbAuthRepository.logout()).thenReturn(apiError.left() as Either<NetworkError, Unit>)

        // Expected behavior:
        // - Should emit LogoutResult.Error with status 401 and message "Unauthorized"
        // - Should call WorkerManager.cancelTimer()
        // - Should call session.clear()
        // - Should NOT call mbCoreCommonDataSource.logout()
        // - Should track analytics events (initial + error)
        
        // Example assertion (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
         val result = manager.logout().first()
         assertTrue(result is LogoutResult.Error)
         val errorResult = result as LogoutResult.Error
         assertEquals(401, errorResult.status)
         assertEquals("Unauthorized", errorResult.message)
         verify(mbAuthRepository).logout()
         verify(mockSession).clear()
         verify(mbCoreCommonDataSource, never()).logout()
    }

    @Test
    fun `logout should emit Error when repository logout fails with non-ApiError`() = runTest {
        // Given: Repository returns non-ApiError (e.g., KtorError)
        val networkError = NetworkError.KtorError(Throwable("Network failure"))
        whenever(mbAuthRepository.logout()).thenReturn(networkError.left() as Either<NetworkError, Unit>)

        // Expected behavior:
        // - Should emit LogoutResult.Error with UnknownError status
        // - Should call WorkerManager.cancelTimer()
        // - Should call session.clear()
        // - Should NOT call mbCoreCommonDataSource.logout()
        
        // Example assertion (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
         val result = manager.logout().first()
         assertTrue(result is LogoutResult.Error)
         val errorResult = result as LogoutResult.Error
         assertEquals(
             AuthErrorCodeResponse.UnknownError.code,
             errorResult.status
         )
         assertEquals(
             AuthErrorCodeResponse.UnknownError.description,
             errorResult.message
         )
         verify(mbAuthRepository).logout()
         verify(mockSession).clear()
         verify(mbCoreCommonDataSource, never()).logout()
    }

    @Test
    fun `logout should track analytics events on success`() = runTest {
        // Given: Repository returns success
        whenever(mbAuthRepository.logout()).thenReturn(Unit.right())

        // Expected behavior:
        // - Should track "User initiated logout" event
        // - Should track "User logged out successfully" event
        
        // Example verification (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
        // Collect the flow to ensure all side effects (analytics calls) complete
        // Use toList() to collect all emissions and ensure flow completes fully
        val results = manager.logout().toList()
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LogoutResult.Success)
        // Verify analytics was called twice: initial call (before fold) + success call (before emit in wrapper)
        verify(mixpanel, times(2)).trackMap(any(), any())
        // Verify the initial call
        verify(mixpanel).trackMap(
            eq(LogoutAnalytics.eventName),
            eq(mapOf(LogoutAnalytics.message to "User initiated logout"))
        )
        // Verify the success call
        verify(mixpanel).trackMap(
            eq(LogoutAnalytics.eventName),
            eq(mapOf(LogoutAnalytics.failure to "User logged out successfully"))
        )
    }

    @Test
    fun `logout should track analytics events on error`() = runTest {
        // Given: Repository returns error
        val error = NetworkError.ApiError(
            statusCode = 400,
            errorBody = MbSdkErrorResponse(
                status = 400,
                message = "Bad Request"
            )
        )
        whenever(mbAuthRepository.logout()).thenReturn(error.left() as Either<NetworkError, Unit>)

        // Expected behavior:
        // - Should track "User initiated logout" event
        // - Should track "User logged out unsuccessfully" event with error message
        
        // Example verification (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
        // Collect the flow to ensure all side effects (analytics calls) complete
        val results = manager.logout().toList()
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LogoutResult.Error)
        // Verify analytics was called twice: initial call + error call
        verify(mixpanel, times(2)).trackMap(any(), any())
    }

    @Test
    fun `logout should clear session on success`() = runTest {
        // Given: Repository returns success
        whenever(mbAuthRepository.logout()).thenReturn(Unit.right())

        // Expected behavior:
        // - session.clear() should be called
        
        // Example verification (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
         manager.logout().first()
         verify(mockSession).clear()
    }

    @Test
    fun `logout should clear session on error`() = runTest {
        // Given: Repository returns error
        val error = NetworkError.ApiError(
            statusCode = 400,
            errorBody = MbSdkErrorResponse(
                status = 400,
                message = "Bad Request"
            )
        )
        whenever(mbAuthRepository.logout()).thenReturn(error.left() as Either<NetworkError, Unit>)

        // Expected behavior:
        // - session.clear() should be called even on error
        
        // Example verification (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
         manager.logout().first()
         verify(mockSession).clear()
    }

    @Test
    fun `logout should call mbCoreCommonDataSource logout only on success`() = runTest {
        // Given: Repository returns success
        whenever(mbAuthRepository.logout()).thenReturn(Unit.right())

        // Expected behavior:
        // - mbCoreCommonDataSource.logout() should be called on success
        
        // Example verification (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
         manager.logout().first()
         verify(mbCoreCommonDataSource).logout()
        
        // Reset mocks to clear invocation history before testing error case
        reset(mbCoreCommonDataSource, mbAuthRepository, mockSession, mixpanel)
        
        // And: Should NOT be called on error
         whenever(mbAuthRepository.logout()).thenReturn(
             NetworkError.KtorError(Throwable("")).left() as Either<NetworkError, Unit>
         )
         manager.logout().first()
         verify(mbCoreCommonDataSource, never()).logout()
    }

    @Test
    fun `logout should not call mbCoreCommonDataSource logout on error`() = runTest {
        // Given: Repository returns error
        val error = NetworkError.ApiError(
            statusCode = 500,
            errorBody = MbSdkErrorResponse(
                status = 500,
                message = "Server Error"
            )
        )
        whenever(mbAuthRepository.logout()).thenReturn(error.left() as Either<NetworkError, Unit>)

        // Expected behavior:
        // - mbCoreCommonDataSource.logout() should NOT be called on error
        
        // Example verification (when testable):
        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )
         manager.logout().first()
         verify(mbCoreCommonDataSource, never()).logout()
    }
}

