package com.appmb.sdk.mbauth.core.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.model.LogoutResult
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for MbAuthManagerImpl.deactivateAccount() method.
 * 
 * Uses a testable wrapper class that accepts dependencies via constructor injection
 * to enable proper unit testing.
 */
@RunWith(MockitoJUnitRunner::class)
class MbAuthManagerImplDeactivateAccountTest {

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
    fun `deactivateAccount should emit Success when repository deactivateAccount succeeds`() = runTest {
        // Given: Repository returns success
        whenever(mbAuthRepository.deactivateAccount()).thenReturn(Unit.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: deactivateAccount is called
        val result = manager.deactivateAccount().first()

        // Then: Should emit Success
        assertTrue(result is DeactivateAccountResult.Success)

        // Verify interactions
        verify(mbAuthRepository).deactivateAccount()
        verify(mbCoreCommonDataSource).logout()
        verify(mockSession).clear()
    }

    @Test
    fun `deactivateAccount should emit Error when repository deactivateAccount fails with ApiError`() = runTest {
        // Given: Repository returns ApiError
        val apiError = NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
                status = 401,
                message = "Unauthorized"
            )
        )
        whenever(mbAuthRepository.deactivateAccount()).thenReturn(apiError.left() as Either<NetworkError, Unit>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: deactivateAccount is called
        val result = manager.deactivateAccount().first()

        // Then: Should emit Error with correct status and message
        assertTrue(result is DeactivateAccountResult.Error)
        val errorResult = result as DeactivateAccountResult.Error
        assertEquals(401, errorResult.code)
        assertEquals("Unauthorized", errorResult.message)

        // Verify interactions
        verify(mbAuthRepository).deactivateAccount()
        verify(mbCoreCommonDataSource, never()).logout()
        verify(mockSession, never()).clear()
    }

    @Test
    fun `deactivateAccount should emit Error when repository deactivateAccount fails with non-ApiError`() = runTest {
        // Given: Repository returns non-ApiError (e.g., KtorError)
        val networkError = NetworkError.KtorError(Throwable("Network failure"))
        whenever(mbAuthRepository.deactivateAccount()).thenReturn(networkError.left() as Either<NetworkError, Unit>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: deactivateAccount is called
        val result = manager.deactivateAccount().first()

        // Then: Should emit Error with UnknownError status
        assertTrue(result is DeactivateAccountResult.Error)
        val errorResult = result as DeactivateAccountResult.Error
        assertEquals(
            AuthErrorCodeResponse.UnknownError.code,
            errorResult.code
        )
        assertEquals(
            AuthErrorCodeResponse.UnknownError.description,
            errorResult.message
        )

        // Verify interactions
        verify(mbAuthRepository).deactivateAccount()
        verify(mbCoreCommonDataSource, never()).logout()
        verify(mockSession, never()).clear()
    }

    @Test
    fun `deactivateAccount should call mbCoreCommonDataSource logout on success`() = runTest {
        // Given: Repository returns success
        whenever(mbAuthRepository.deactivateAccount()).thenReturn(Unit.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: deactivateAccount is called
        manager.deactivateAccount().first()

        // Then: mbCoreCommonDataSource.logout() should be called
        verify(mbCoreCommonDataSource).logout()
    }

    @Test
    fun `deactivateAccount should not call mbCoreCommonDataSource logout on error`() = runTest {
        // Given: Repository returns error
        val error = NetworkError.ApiError(
            statusCode = 500,
            errorBody = MbSdkErrorResponse(
                status = 500,
                message = "Server Error"
            )
        )
        whenever(mbAuthRepository.deactivateAccount()).thenReturn(error.left() as Either<NetworkError, Unit>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: deactivateAccount is called
        manager.deactivateAccount().first()

        // Then: mbCoreCommonDataSource.logout() should NOT be called
        verify(mbCoreCommonDataSource, never()).logout()
    }

    @Test
    fun `deactivateAccount should clear session on success`() = runTest {
        // Given: Repository returns success
        whenever(mbAuthRepository.deactivateAccount()).thenReturn(Unit.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: deactivateAccount is called
        manager.deactivateAccount().first()

        // Then: Session should be cleared
        verify(mockSession).clear()
    }

    @Test
    fun `deactivateAccount should not clear session on error`() = runTest {
        // Given: Repository returns error
        val error = NetworkError.ApiError(
            statusCode = 400,
            errorBody = MbSdkErrorResponse(
                status = 400,
                message = "Bad Request"
            )
        )
        whenever(mbAuthRepository.deactivateAccount()).thenReturn(error.left() as Either<NetworkError, Unit>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: deactivateAccount is called
        manager.deactivateAccount().first()

        // Then: Session should NOT be cleared on error
        verify(mockSession, never()).clear()
    }
}

