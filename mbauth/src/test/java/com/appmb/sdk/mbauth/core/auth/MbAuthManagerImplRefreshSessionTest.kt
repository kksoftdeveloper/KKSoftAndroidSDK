package com.appmb.sdk.mbauth.core.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.event.RefreshTokenAnalytics
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.domain.auth.MbCoreAuthRepository
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.model.MbSdkErrorResponse
import com.appmb.sdk.mbcore.session.MbAuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for MbAuthManagerImpl.refreshSession() method.
 * 
 * Uses a testable wrapper class that accepts dependencies via constructor injection
 * to enable proper unit testing.
 */
@RunWith(MockitoJUnitRunner::class)
class MbAuthManagerImplRefreshSessionTest {

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
    fun `refreshSession should emit Success when repository refreshToken succeeds`() = runTest {
        // Given: Repository returns success with refresh token response
        val refreshTokenResponse = MbRefreshTokenResponse(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            expireDate = "2024-12-31T23:59:59Z"
        )
        whenever(mbCoreAuthRepository.refreshToken()).thenReturn(refreshTokenResponse.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: refreshSession is called
        val result = manager.refreshSession().first()

        // Then: Should emit Success with correct token data
        assertTrue(result is LoginResult.Success)
        val successResult = result as LoginResult.Success
        assertEquals("new-access-token", successResult.data.accessToken)
        assertEquals("new-refresh-token", successResult.data.refreshToken)
        assertEquals("2024-12-31T23:59:59Z", successResult.data.expireDate)

        // Verify interactions
        verify(mbCoreAuthRepository).refreshToken()
    }

    @Test
    fun `refreshSession should emit Error when repository refreshToken fails with ApiError`() = runTest {
        // Given: Repository returns ApiError
        val apiError = NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
                status = 401,
                message = "Unauthorized"
            )
        )
        whenever(mbCoreAuthRepository.refreshToken()).thenReturn(apiError.left() as Either<NetworkError, MbRefreshTokenResponse?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: refreshSession is called
        val result = manager.refreshSession().first()

        // Then: Should emit Error with correct status and message
        assertTrue(result is LoginResult.Error)
        val errorResult = result as LoginResult.Error
        assertEquals(401, errorResult.status)
        assertEquals("Unauthorized", errorResult.message)

        // Verify interactions
        verify(mbCoreAuthRepository).refreshToken()
    }

    @Test
    fun `refreshSession should emit Error when repository refreshToken fails with non-ApiError`() = runTest {
        // Given: Repository returns non-ApiError (e.g., KtorError)
        val networkError = NetworkError.KtorError(Throwable("Network failure"))
        whenever(mbCoreAuthRepository.refreshToken()).thenReturn(networkError.left() as Either<NetworkError, MbRefreshTokenResponse?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: refreshSession is called
        val result = manager.refreshSession().first()

        // Then: Should emit Error with UnknownError status
        assertTrue(result is LoginResult.Error)
        val errorResult = result as LoginResult.Error
        assertEquals(
            AuthErrorCodeResponse.UnknownError.code,
            errorResult.status
        )
        assertEquals(
            AuthErrorCodeResponse.UnknownError.description,
            errorResult.message
        )

        // Verify interactions
        verify(mbCoreAuthRepository).refreshToken()
    }

    @Test
    fun `refreshSession should handle null response from repository`() = runTest {
        // Given: Repository returns null response
        whenever(mbCoreAuthRepository.refreshToken()).thenReturn(null.right() as Either<NetworkError, MbRefreshTokenResponse?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: refreshSession is called
        val results = manager.refreshSession().toList()

        // Then: Should not emit any result (null response is ignored)
        assertTrue(results.isEmpty())

        // Verify interactions
        verify(mbCoreAuthRepository).refreshToken()
    }

    @Test
    fun `refreshSession should track analytics events on success`() = runTest {
        // Given: Repository returns success
        val refreshTokenResponse = MbRefreshTokenResponse(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            expireDate = "2024-12-31T23:59:59Z"
        )
        whenever(mbCoreAuthRepository.refreshToken()).thenReturn(refreshTokenResponse.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: refreshSession is called
        manager.refreshSession().toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        // Verify the initial call
        verify(mixpanel).trackMap(
            eq(RefreshTokenAnalytics.eventName),
            eq(mapOf(RefreshTokenAnalytics.message to "User initiated refresh token"))
        )
        // Verify the success call
        verify(mixpanel).trackMap(
            eq(RefreshTokenAnalytics.eventName),
            eq(mapOf(RefreshTokenAnalytics.success to "User refresh token successful"))
        )
    }

    @Test
    fun `refreshSession should track analytics events on error`() = runTest {
        // Given: Repository returns error
        val error = NetworkError.ApiError(
            statusCode = 400,
            errorBody = MbSdkErrorResponse(
                status = 400,
                message = "Bad Request"
            )
        )
        whenever(mbCoreAuthRepository.refreshToken()).thenReturn(error.left() as Either<NetworkError, MbRefreshTokenResponse?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: refreshSession is called
        manager.refreshSession().toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        // Verify the initial call
        verify(mixpanel).trackMap(
            eq(RefreshTokenAnalytics.eventName),
            eq(mapOf(RefreshTokenAnalytics.message to "User initiated refresh token"))
        )
        // Verify the error call
        verify(mixpanel).trackMap(
            eq(RefreshTokenAnalytics.eventName),
            eq(mapOf(RefreshTokenAnalytics.failure to "User refresh token failed"))
        )
    }

    @Test
    fun `refreshSession should create MbAuthData with correct properties on success`() = runTest {
        // Given: Repository returns success with all token fields
        val refreshTokenResponse = MbRefreshTokenResponse(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            expireDate = "2025-01-01T00:00:00Z"
        )
        whenever(mbCoreAuthRepository.refreshToken()).thenReturn(refreshTokenResponse.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: refreshSession is called
        val result = manager.refreshSession().first()

        // Then: Should create MbAuthData with all properties from response
        assertTrue(result is LoginResult.Success)
        val successResult = result as LoginResult.Success
        val authData = successResult.data
        assertEquals("access-token-123", authData.accessToken)
        assertEquals("refresh-token-456", authData.refreshToken)
        assertEquals("2025-01-01T00:00:00Z", authData.expireDate)
    }
}

