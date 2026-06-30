package com.appmb.sdk.mbauth.core.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.event.SignupAnalytics
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.domain.auth.MbCoreAuthRepository
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for MbAuthManagerImpl.register() method.
 * 
 * Uses a testable wrapper class that accepts dependencies via constructor injection
 * to enable proper unit testing.
 */
@RunWith(MockitoJUnitRunner::class)
class MbAuthManagerImplRegisterTest {

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

    @Mock
    private lateinit var mockAuthProvider: MbAuthProvider

    private lateinit var authParams: MbAuthParams

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authParams = MbAuthParams.Builder(
            type = LoginType.PHONE,
            phone = "1234567890",
            password = "password123"
        ).build()
    }

    @After
    fun tearDown() {
        // Clean up if needed
    }

    @Test
    fun `register should emit Success when provider register succeeds with gameUuid`() = runTest {
        // Given: Provider returns success with gameUuid
        val registerDataModel = RegisterDataModel(
            accessToken = "register-access-token-123",
            refreshToken = "register-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = "register-game-uuid-789",
            serverId = "server-1",
            isGuest = false
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(registerDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        val result = manager.register(authParams).first()

        // Then: Should emit Success with correct data
        assertTrue(result is RegisterResult.Success)
        val successResult = result as RegisterResult.Success
        assertEquals("register-access-token-123", successResult.data.accessToken)
        assertEquals("register-refresh-token-456", successResult.data.refreshToken)
        assertEquals("register-game-uuid-789", successResult.data.gameUuid)
        assertEquals("server-1", successResult.data.serverId)

        // Verify interactions
        verify(mockAuthProvider).register(authParams)
        verify(mockSession).clear() // Session is cleared before saving new one
        verify(mockSession).save(eq(successResult.data))
    }

    @Test
    fun `register should emit UnknownServerConfiguration when provider register succeeds without gameUuid`() = runTest {
        // Given: Provider returns success without gameUuid
        val registerDataModel = RegisterDataModel(
            accessToken = "register-access-token-123",
            refreshToken = "register-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = null, // Empty gameUuid
            serverId = "server-1",
            isGuest = false
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(registerDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        val result = manager.register(authParams).first()

        // Then: Should emit UnknownServerConfiguration
        assertTrue(result is RegisterResult.UnknownServerConfiguration)
        val configResult = result as RegisterResult.UnknownServerConfiguration
        assertEquals("register-access-token-123", configResult.data.accessToken)
        assertTrue(configResult.data.gameUuid.isNullOrEmpty())

        // Verify interactions
        verify(mockAuthProvider).register(authParams)
        verify(mockSession).clear() // Session is cleared before saving new one
        verify(mockSession).save(eq(configResult.data))
    }

    @Test
    fun `register should emit Error when provider register fails with ApiError`() = runTest {
        // Given: Provider returns ApiError
        val apiError = NetworkError.ApiError(
            statusCode = 400,
            errorBody = MbSdkErrorResponse(
                status = 400,
                message = "Registration failed"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(apiError.left() as Either<NetworkError, RegisterDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        val results = manager.register(authParams).toList()

        // Then: Should emit Error with correct status and message
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is RegisterResult.Error)
        val errorResult = results.first() as RegisterResult.Error
        assertEquals(400, errorResult.code)
        assertEquals("Registration failed", errorResult.message)

        // Verify interactions
        verify(mockAuthProvider).register(authParams)
        verify(mockSession).clear()
        verify(mockSession, never()).save(any())
    }

    @Test
    fun `register should emit Error when provider register fails with non-ApiError`() = runTest {
        // Given: Provider returns non-ApiError (e.g., KtorError)
        val networkError = NetworkError.KtorError(Throwable("Network failure"))
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(networkError.left() as Either<NetworkError, RegisterDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        val results = manager.register(authParams).toList()

        // Then: Should emit Error with UnknownError status
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is RegisterResult.Error)
        val errorResult = results.first() as RegisterResult.Error
        assertEquals(
            AuthErrorCodeResponse.UnknownError.code,
            errorResult.code
        )
        assertEquals(
            AuthErrorCodeResponse.UnknownError.description,
            errorResult.message
        )

        // Verify interactions
        verify(mockAuthProvider).register(authParams)
        verify(mockSession).clear()
        verify(mockSession, never()).save(any())
    }

    @Test
    fun `register should handle null response from provider`() = runTest {
        // Given: Provider returns null response
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(null.right() as Either<NetworkError, RegisterDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        val results = manager.register(authParams).toList()

        // Then: Should not emit any result (null response is ignored)
        // Note: Session is still cleared even when data is null, but no new session is saved
        assertTrue(results.isEmpty())

        // Verify interactions
        verify(mockAuthProvider).register(authParams)
        verify(mockSession).clear() // Session is cleared even when data is null
        verify(mockSession, never()).save(any()) // But new session is not saved
    }

    @Test
    fun `register should return early when authParams type is null`() = runTest {
        // Given: authParams with null type
        val paramsWithoutType = MbAuthParams.Builder(
            phone = "1234567890",
            password = "password123"
        ).build()
        
        // Use reflection to set type to null for testing
        val typeField = MbAuthParams::class.java.getDeclaredField("type")
        typeField.isAccessible = true
        typeField.set(paramsWithoutType, null)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        val results = manager.register(paramsWithoutType).toList()

        // Then: Should not emit any result and return early
        assertTrue(results.isEmpty())

        // Verify provider is never called
        verify(mbAuthProviderFactory, never()).getProvider(any())
        verify(mockAuthProvider, never()).register(any())
    }

    @Test
    fun `register should track analytics events on success`() = runTest {
        // Given: Provider returns success
        val registerDataModel = RegisterDataModel(
            accessToken = "register-access-token-123",
            refreshToken = "register-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = "register-game-uuid-789"
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(registerDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        manager.register(authParams).toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(SignupAnalytics.phoneSignup),
            eq(mapOf(SignupAnalytics.success to "Sign up successfully completed"))
        )
    }

    @Test
    fun `register should track analytics events on error`() = runTest {
        // Given: Provider returns error
        val error = NetworkError.ApiError(
            statusCode = 400,
            errorBody = MbSdkErrorResponse(
                status = 400,
                message = "Registration failed"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(error.left() as Either<NetworkError, RegisterDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        manager.register(authParams).toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(SignupAnalytics.phoneSignup),
            eq(mapOf(SignupAnalytics.failure to "Signup failed"))
        )
    }

    @Test
    fun `register should track analytics events on UnknownServerConfiguration`() = runTest {
        // Given: Provider returns success without gameUuid
        val registerDataModel = RegisterDataModel(
            accessToken = "register-access-token-123",
            refreshToken = "register-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = null
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(registerDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        manager.register(authParams).toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(SignupAnalytics.phoneSignup),
            eq(mapOf(SignupAnalytics.success to "Game UUID is null or empty"))
        )
    }

    @Test
    fun `register should clear session before saving new session`() = runTest {
        // Given: Provider returns success
        val registerDataModel = RegisterDataModel(
            accessToken = "register-access-token-123",
            refreshToken = "register-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = "register-game-uuid-789"
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(registerDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        val result = manager.register(authParams).first()

        // Then: Session should be cleared before saving
        assertTrue(result is RegisterResult.Success)
        val successResult = result as RegisterResult.Success
        verify(mockSession).clear() // Cleared first
        verify(mockSession).save(eq(successResult.data)) // Then saved with new data
    }

    @Test
    fun `register should clear session on error`() = runTest {
        // Given: Provider returns error
        val error = NetworkError.ApiError(
            statusCode = 500,
            errorBody = MbSdkErrorResponse(
                status = 500,
                message = "Server Error"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.register(authParams)).thenReturn(error.left() as Either<NetworkError, RegisterDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: register is called
        manager.register(authParams).toList()

        // Then: Session should be cleared on error
        verify(mockSession).clear()
        verify(mockSession, never()).save(any())
    }
}

