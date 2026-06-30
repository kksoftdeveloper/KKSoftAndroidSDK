package com.appmb.sdk.mbauth.core.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
import com.appmb.sdk.mbauth.event.toAnalyticsEventName
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for MbAuthManagerImpl.login() method.
 * 
 * Uses a testable wrapper class that accepts dependencies via constructor injection
 * to enable proper unit testing.
 */
@RunWith(MockitoJUnitRunner::class)
class MbAuthManagerImplLoginTest {

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
    fun `login should emit Success when provider login succeeds with gameUuid`() = runTest {
        // Given: Provider returns success with gameUuid
        val loginDataModel = LoginDataModel(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = "game-uuid-789",
            serverId = "server-1",
            isGuest = false
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(authParams).first()

        // Then: Should emit Success with correct data
        assertTrue(result is LoginResult.Success)
        val successResult = result as LoginResult.Success
        assertEquals("access-token-123", successResult.data.accessToken)
        assertEquals("refresh-token-456", successResult.data.refreshToken)
        assertEquals("game-uuid-789", successResult.data.gameUuid)
        assertEquals("server-1", successResult.data.serverId)

        // Verify interactions
        verify(mockAuthProvider).login(authParams)
        verify(mockSession).save(eq(successResult.data))
    }

    @Test
    fun `login should emit UnknownServerConfiguration when provider login succeeds without gameUuid`() = runTest {
        // Given: Provider returns success without gameUuid
        val loginDataModel = LoginDataModel(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = null, // Empty gameUuid
            serverId = "server-1",
            isGuest = false
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(authParams).first()

        // Then: Should emit UnknownServerConfiguration
        assertTrue(result is LoginResult.UnknownServerConfiguration)
        val configResult = result as LoginResult.UnknownServerConfiguration
        assertEquals("access-token-123", configResult.data.accessToken)
        assertTrue(configResult.data.gameUuid.isNullOrEmpty())

        // Verify interactions
        verify(mockAuthProvider).login(authParams)
        verify(mockSession).save(eq(configResult.data))
    }

    @Test
    fun `login should emit Error when provider login fails with ApiError`() = runTest {
        // Given: Provider returns ApiError
        val apiError = NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
                status = 401,
                message = "Unauthorized"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(apiError.left() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(authParams).toList()

        // Then: Should emit Error with correct status and message
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LoginResult.Error)
        val errorResult = results.first() as LoginResult.Error
        assertEquals(401, errorResult.status)
        assertEquals("Unauthorized", errorResult.message)

        // Verify interactions
        verify(mockAuthProvider).login(authParams)
        verify(mockSession).clear()
        verify(mockSession, never()).save(any())
    }

    @Test
    fun `login should emit Error when provider login fails with non-ApiError`() = runTest {
        // Given: Provider returns non-ApiError (e.g., KtorError)
        val networkError = NetworkError.KtorError(Throwable("Network failure"))
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(networkError.left() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(authParams).toList()

        // Then: Should emit Error with UnknownError status
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LoginResult.Error)
        val errorResult = results.first() as LoginResult.Error
        assertEquals(
            AuthErrorCodeResponse.UnknownError.code,
            errorResult.status
        )
        assertEquals(
            AuthErrorCodeResponse.UnknownError.description,
            errorResult.message
        )

        // Verify interactions
        verify(mockAuthProvider).login(authParams)
        verify(mockSession).clear()
    }

    @Test
    fun `login should handle null response from provider`() = runTest {
        // Given: Provider returns null response
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(null.right() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(authParams).toList()

        // Then: Should not emit any result (null response is ignored)
        assertTrue(results.isEmpty())

        // Verify interactions
        verify(mockAuthProvider).login(authParams)
        verify(mockSession, never()).save(any())
        verify(mockSession, never()).clear()
    }

    @Test
    fun `login should return early when authParams type is null`() = runTest {
        // Given: authParams with null type
        // Note: Since Builder always sets a type (defaults to PHONE), we need to use reflection
        // to set type to null for testing purposes
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

        // When: login is called
        val results = manager.login(paramsWithoutType).toList()

        // Then: Should not emit any result and return early
        assertTrue(results.isEmpty())

        // Verify provider is never called
        verify(mbAuthProviderFactory, never()).getProvider(any())
        verify(mockAuthProvider, never()).login(any())
    }

    @Test
    fun `login should track analytics events on success`() = runTest {
        // Given: Provider returns success
        val loginDataModel = LoginDataModel(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            gameUuid = "game-uuid-789"
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        manager.login(authParams).toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(authParams.toAnalyticsEventName()),
            eq(mapOf("success" to "${authParams.toAnalyticsEventName()} successfully logged in"))
        )
    }

    @Test
    fun `login should track analytics events on error`() = runTest {
        // Given: Provider returns error
        val error = NetworkError.ApiError(
            statusCode = 400,
            errorBody = MbSdkErrorResponse(
                status = 400,
                message = "Bad Request"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(error.left() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        manager.login(authParams).toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(authParams.toAnalyticsEventName()),
            eq(mapOf("failure" to "Bad Request"))
        )
    }

    @Test
    fun `login should track analytics events on UnknownServerConfiguration`() = runTest {
        // Given: Provider returns success without gameUuid
        val loginDataModel = LoginDataModel(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            gameUuid = null
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        manager.login(authParams).toList() // Collect the flow to ensure all side effects complete

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(authParams.toAnalyticsEventName()),
            eq(mapOf("failure" to "Game UUID is null or empty"))
        )
    }

    @Test
    fun `login should save session data on success`() = runTest {
        // Given: Provider returns success
        val loginDataModel = LoginDataModel(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            gameUuid = "game-uuid-789"
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.PHONE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(authParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(authParams).first()

        // Then: Session should be saved with correct data
        assertTrue(result is LoginResult.Success)
        val successResult = result as LoginResult.Success
        verify(mockSession).save(eq(successResult.data))
    }

    // ==================== Facebook Login Tests ====================

    @Test
    fun `login with Facebook should emit Success when provider login succeeds with gameUuid`() = runTest {
        // Given: Facebook login params
        val facebookAuthParams = MbAuthParams.Builder(
            type = LoginType.FACEBOOK,
            context = null, // Context not needed for unit tests
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "fb-access-token-123",
            refreshToken = "fb-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = "fb-game-uuid-789",
            serverId = "server-1",
            isGuest = false
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.FACEBOOK)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(facebookAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(facebookAuthParams).first()

        // Then: Should emit Success with correct data
        assertTrue(result is LoginResult.Success)
        val successResult = result as LoginResult.Success
        assertEquals("fb-access-token-123", successResult.data.accessToken)
        assertEquals("fb-refresh-token-456", successResult.data.refreshToken)
        assertEquals("fb-game-uuid-789", successResult.data.gameUuid)

        // Verify interactions
        verify(mockAuthProvider).login(facebookAuthParams)
        verify(mockSession).save(eq(successResult.data))
    }

    @Test
    fun `login with Facebook should emit Error when provider login fails`() = runTest {
        // Given: Facebook login params
        val facebookAuthParams = MbAuthParams.Builder(
            type = LoginType.FACEBOOK,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val apiError = NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
                status = 401,
                message = "Facebook authentication failed"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.FACEBOOK)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(facebookAuthParams)).thenReturn(apiError.left() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(facebookAuthParams).toList()

        // Then: Should emit Error
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LoginResult.Error)
        val errorResult = results.first() as LoginResult.Error
        assertEquals(401, errorResult.status)
        assertEquals("Facebook authentication failed", errorResult.message)

        // Verify interactions
        verify(mockAuthProvider).login(facebookAuthParams)
        verify(mockSession).clear()
    }

    @Test
    fun `login with Facebook should track analytics events on success`() = runTest {
        // Given: Facebook login params
        val facebookAuthParams = MbAuthParams.Builder(
            type = LoginType.FACEBOOK,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "fb-access-token-123",
            refreshToken = "fb-refresh-token-456",
            gameUuid = "fb-game-uuid-789"
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.FACEBOOK)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(facebookAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        manager.login(facebookAuthParams).toList()

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(facebookAuthParams.toAnalyticsEventName()),
            eq(mapOf("success" to "${facebookAuthParams.toAnalyticsEventName()} successfully logged in"))
        )
    }

    // ==================== Google Login Tests ====================

    @Test
    fun `login with Google should emit Success when provider login succeeds with gameUuid`() = runTest {
        // Given: Google login params
        val googleAuthParams = MbAuthParams.Builder(
            type = LoginType.GOOGLE,
            context = null, // Context not needed for unit tests
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app",
            googleAccount = null // GoogleSignInAccount not needed for unit tests
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "google-access-token-123",
            refreshToken = "google-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = "google-game-uuid-789",
            serverId = "server-1",
            isGuest = false
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GOOGLE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(googleAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(googleAuthParams).first()

        // Then: Should emit Success with correct data
        assertTrue(result is LoginResult.Success)
        val successResult = result as LoginResult.Success
        assertEquals("google-access-token-123", successResult.data.accessToken)
        assertEquals("google-refresh-token-456", successResult.data.refreshToken)
        assertEquals("google-game-uuid-789", successResult.data.gameUuid)

        // Verify interactions
        verify(mockAuthProvider).login(googleAuthParams)
        verify(mockSession).save(eq(successResult.data))
    }

    @Test
    fun `login with Google should emit Error when provider login fails`() = runTest {
        // Given: Google login params
        val googleAuthParams = MbAuthParams.Builder(
            type = LoginType.GOOGLE,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app",
            googleAccount = null
        ).build()

        val apiError = NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
                status = 401,
                message = "Google authentication failed"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GOOGLE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(googleAuthParams)).thenReturn(apiError.left() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(googleAuthParams).toList()

        // Then: Should emit Error
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LoginResult.Error)
        val errorResult = results.first() as LoginResult.Error
        assertEquals(401, errorResult.status)
        assertEquals("Google authentication failed", errorResult.message)

        // Verify interactions
        verify(mockAuthProvider).login(googleAuthParams)
        verify(mockSession).clear()
    }

    @Test
    fun `login with Google should emit UnknownServerConfiguration when gameUuid is empty`() = runTest {
        // Given: Google login params
        val googleAuthParams = MbAuthParams.Builder(
            type = LoginType.GOOGLE,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app",
            googleAccount = null
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "google-access-token-123",
            refreshToken = "google-refresh-token-456",
            gameUuid = null // Empty gameUuid
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GOOGLE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(googleAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(googleAuthParams).first()

        // Then: Should emit UnknownServerConfiguration
        assertTrue(result is LoginResult.UnknownServerConfiguration)
        val configResult = result as LoginResult.UnknownServerConfiguration
        assertEquals("google-access-token-123", configResult.data.accessToken)
        assertTrue(configResult.data.gameUuid.isNullOrEmpty())

        // Verify interactions
        verify(mockAuthProvider).login(googleAuthParams)
        verify(mockSession).save(eq(configResult.data))
    }

    @Test
    fun `login with Google should track analytics events on success`() = runTest {
        // Given: Google login params
        val googleAuthParams = MbAuthParams.Builder(
            type = LoginType.GOOGLE,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app",
            googleAccount = null
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "google-access-token-123",
            refreshToken = "google-refresh-token-456",
            gameUuid = "google-game-uuid-789"
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GOOGLE)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(googleAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        manager.login(googleAuthParams).toList()

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(googleAuthParams.toAnalyticsEventName()),
            eq(mapOf("success" to "${googleAuthParams.toAnalyticsEventName()} successfully logged in"))
        )
    }

    @Test
    fun `login with Facebook should emit UnknownServerConfiguration when gameUuid is empty`() = runTest {
        // Given: Facebook login params
        val facebookAuthParams = MbAuthParams.Builder(
            type = LoginType.FACEBOOK,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "fb-access-token-123",
            refreshToken = "fb-refresh-token-456",
            gameUuid = null // Empty gameUuid
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.FACEBOOK)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(facebookAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(facebookAuthParams).first()

        // Then: Should emit UnknownServerConfiguration
        assertTrue(result is LoginResult.UnknownServerConfiguration)
        val configResult = result as LoginResult.UnknownServerConfiguration
        assertEquals("fb-access-token-123", configResult.data.accessToken)
        assertTrue(configResult.data.gameUuid.isNullOrEmpty())

        // Verify interactions
        verify(mockAuthProvider).login(facebookAuthParams)
        verify(mockSession).save(eq(configResult.data))
    }

    // ==================== Guest Login Tests ====================

    @Test
    fun `login with Guest should emit Success when provider login succeeds with gameUuid`() = runTest {
        // Given: Guest login params
        val guestAuthParams = MbAuthParams.Builder(
            type = LoginType.GUEST,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "guest-access-token-123",
            refreshToken = "guest-refresh-token-456",
            expireDate = "2024-12-31T23:59:59Z",
            gameUuid = "guest-game-uuid-789",
            serverId = "server-1",
            isGuest = true
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GUEST)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(guestAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(guestAuthParams).first()

        // Then: Should emit Success with correct data
        assertTrue(result is LoginResult.Success)
        val successResult = result as LoginResult.Success
        assertEquals("guest-access-token-123", successResult.data.accessToken)
        assertEquals("guest-refresh-token-456", successResult.data.refreshToken)
        assertEquals("guest-game-uuid-789", successResult.data.gameUuid)
        assertEquals(true, successResult.data.isGuest)

        // Verify interactions
        verify(mockAuthProvider).login(guestAuthParams)
        verify(mockSession).save(eq(successResult.data))
    }

    @Test
    fun `login with Guest should emit Error when provider login fails`() = runTest {
        // Given: Guest login params
        val guestAuthParams = MbAuthParams.Builder(
            type = LoginType.GUEST,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val apiError = NetworkError.ApiError(
            statusCode = 401,
            errorBody = MbSdkErrorResponse(
                status = 401,
                message = "Guest authentication failed"
            )
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GUEST)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(guestAuthParams)).thenReturn(apiError.left() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(guestAuthParams).toList()

        // Then: Should emit Error
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LoginResult.Error)
        val errorResult = results.first() as LoginResult.Error
        assertEquals(401, errorResult.status)
        assertEquals("Guest authentication failed", errorResult.message)

        // Verify interactions
        verify(mockAuthProvider).login(guestAuthParams)
        verify(mockSession).clear()
    }

    @Test
    fun `login with Guest should emit UnknownServerConfiguration when gameUuid is empty`() = runTest {
        // Given: Guest login params
        val guestAuthParams = MbAuthParams.Builder(
            type = LoginType.GUEST,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "guest-access-token-123",
            refreshToken = "guest-refresh-token-456",
            gameUuid = null, // Empty gameUuid
            isGuest = true
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GUEST)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(guestAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val result = manager.login(guestAuthParams).first()

        // Then: Should emit UnknownServerConfiguration
        assertTrue(result is LoginResult.UnknownServerConfiguration)
        val configResult = result as LoginResult.UnknownServerConfiguration
        assertEquals("guest-access-token-123", configResult.data.accessToken)
        assertTrue(configResult.data.gameUuid.isNullOrEmpty())
        assertEquals(true, configResult.data.isGuest)

        // Verify interactions
        verify(mockAuthProvider).login(guestAuthParams)
        verify(mockSession).save(eq(configResult.data))
    }

    @Test
    fun `login with Guest should track analytics events on success`() = runTest {
        // Given: Guest login params
        val guestAuthParams = MbAuthParams.Builder(
            type = LoginType.GUEST,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val loginDataModel = LoginDataModel(
            accessToken = "guest-access-token-123",
            refreshToken = "guest-refresh-token-456",
            gameUuid = "guest-game-uuid-789",
            isGuest = true
        )
        whenever(mbAuthProviderFactory.getProvider(LoginType.GUEST)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(guestAuthParams)).thenReturn(loginDataModel.right())

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        manager.login(guestAuthParams).toList()

        // Then: Should track analytics events
        verify(mixpanel).trackMap(
            eq(guestAuthParams.toAnalyticsEventName()),
            eq(mapOf("success" to "${guestAuthParams.toAnalyticsEventName()} successfully logged in"))
        )
    }

    @Test
    fun `login with Guest should handle null response from provider`() = runTest {
        // Given: Guest login params
        val guestAuthParams = MbAuthParams.Builder(
            type = LoginType.GUEST,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        whenever(mbAuthProviderFactory.getProvider(LoginType.GUEST)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(guestAuthParams)).thenReturn(null.right() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(guestAuthParams).toList()

        // Then: Should not emit any result (null response is ignored)
        assertTrue(results.isEmpty())

        // Verify interactions
        verify(mockAuthProvider).login(guestAuthParams)
        verify(mockSession, never()).save(any())
        verify(mockSession, never()).clear()
    }

    @Test
    fun `login with Guest should emit Error when provider login fails with non-ApiError`() = runTest {
        // Given: Guest login params
        val guestAuthParams = MbAuthParams.Builder(
            type = LoginType.GUEST,
            osVersion = "Android 11",
            gameId = 1,
            appVersion = "1.0.0",
            appPackageName = "com.example.app"
        ).build()

        val networkError = NetworkError.KtorError(Throwable("Network failure"))
        whenever(mbAuthProviderFactory.getProvider(LoginType.GUEST)).thenReturn(mockAuthProvider)
        whenever(mockAuthProvider.login(guestAuthParams)).thenReturn(networkError.left() as Either<NetworkError, LoginDataModel?>)

        val manager = TestableMbAuthManagerImpl(
            mbAuthProviderFactory,
            mixpanel,
            mbAuthRepository,
            mbCoreAuthRepository,
            mbCoreCommonDataSource,
            mockSession
        )

        // When: login is called
        val results = manager.login(guestAuthParams).toList()

        // Then: Should emit Error with UnknownError status
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is LoginResult.Error)
        val errorResult = results.first() as LoginResult.Error
        assertEquals(
            AuthErrorCodeResponse.UnknownError.code,
            errorResult.status
        )
        assertEquals(
            AuthErrorCodeResponse.UnknownError.description,
            errorResult.message
        )

        // Verify interactions
        verify(mockAuthProvider).login(guestAuthParams)
        verify(mockSession).clear()
    }
}

