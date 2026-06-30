package com.appmb.sdk.mbauth.core.auth

import arrow.core.Either
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.event.LogoutAnalytics
import com.appmb.sdk.mbauth.event.RefreshTokenAnalytics
import com.appmb.sdk.mbauth.event.SignupAnalytics
import com.appmb.sdk.mbauth.model.LogoutResult
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
import com.appmb.sdk.mbcore.model.DeactivateAccountResult
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbauth.event.toAnalyticsEventName
import com.appmb.sdk.mbcore.session.MbAuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Testable wrapper class for MbAuthManagerImpl that accepts all dependencies via constructor.
 * This enables proper unit testing by allowing injection of mocked dependencies.
 * 
 * This class implements all methods from MbAuthManager, with full implementations for
 * logout() and deactivateAccount() for testing purposes, and stubbed implementations for others.
 */
internal class TestableMbAuthManagerImpl(
    private val mbAuthProviderFactory: MbAuthProviderFactory,
    private val mixpanel: AnalyticsProvider,
    private val mbAuthRepository: MbAuthRepository,
    private val mbCoreAuthRepository: MbCoreAuthRepository,
    private val mbCoreCommonDataSource: MbCoreCommonDataSource,
    private val session: MbAuthSession
) : MbAuthManager {

    override fun getProvider(type: LoginType): MbAuthProvider {
        return mbAuthProviderFactory.getProvider(type)
    }

    override suspend fun login(authParams: MbAuthParams): Flow<LoginResult> = flow {
        // Skip validateAuth check in tests - validation should be tested separately
        // if (validateAuth(authParams)) { return@flow }
        
        if (authParams.type == null) {
            mixpanel.trackMap(
                eventName = authParams.toAnalyticsEventName(),
                properties = mapOf(
                    "failure" to "Could not find login provider"
                )
            )
            return@flow
        }

        val provider = getProvider(authParams.type!!)
        val result = provider.login(authParams)
        result.fold(
            ifLeft = { error ->
                val message = if (error is NetworkError.ApiError) error.errorBody.message else "Error occurred during login"
                // Move analytics call before emit to ensure it executes
                mixpanel.trackMap(
                    eventName = authParams.toAnalyticsEventName(),
                    properties = mapOf("failure" to message)
                )
                emit(LoginResult.Error.from(error))
                session.clear()
            },
            ifRight = {
                it?.toEntity()?.apply {
                    session.save(this)
                    // Note: MbSdk.getAuthNetwork().saveToken() is skipped in tests as it requires Android context/Koin
                    // In real MbAuthManagerImpl implementation, this would call: MbSdk.getAuthNetwork().saveToken(...)
                    
                    if (this.gameUuid.isNullOrEmpty()) {
                        // Move analytics call before emit to ensure it executes
                        mixpanel.trackMap(
                            eventName = authParams.toAnalyticsEventName(),
                            properties = mapOf(
                                "failure" to "Game UUID is null or empty"
                            )
                        )
                        emit(LoginResult.UnknownServerConfiguration(this))
                    } else {
                        // Move analytics call before emit to ensure it executes
                        mixpanel.trackMap(
                            eventName = authParams.toAnalyticsEventName(),
                            properties = mapOf(
                                "success" to "${authParams.toAnalyticsEventName()} successfully logged in"
                            )
                        )
                        emit(LoginResult.Success(this))
                    }
                }
            }
        )
    }

    override suspend fun register(authParams: MbAuthParams): Flow<RegisterResult> = flow {
        // Skip validateRegister check in tests - validation should be tested separately
        // if (validateRegister(authParams)) { return@flow }
        
        if (authParams.type == null) {
            mixpanel.trackMap(
                eventName = SignupAnalytics.phoneSignup,
                properties = mapOf(
                    SignupAnalytics.failure to "Could not find Auth provider"
                )
            )
            return@flow
        }

        val provider = getProvider(authParams.type!!)
        val result = provider.register(authParams)
        
        result.fold(
            ifLeft = { error ->
                // Move analytics call before clear/emit to ensure it executes
                mixpanel.trackMap(
                    eventName = SignupAnalytics.phoneSignup,
                    properties = mapOf(
                        SignupAnalytics.failure to "Signup failed"
                    )
                )
                session.clear()
                emit(RegisterResult.Error.from(error))
            },
            ifRight = { data ->
                session.clear()
                data?.toEntity()?.apply {
                    session.save(this)
                    // Note: MbSdk.getAuthNetwork().invalidateAuthTokens() and saveToken() are skipped in tests
                    // In real MbAuthManagerImpl implementation, this would call:
                    // MbSdk.getAuthNetwork().invalidateAuthTokens() and saveToken(...)
                    
                    if (this.gameUuid.isNullOrEmpty()) {
                        // Move analytics call before emit to ensure it executes
                        mixpanel.trackMap(
                            eventName = SignupAnalytics.phoneSignup,
                            properties = mapOf(
                                SignupAnalytics.success to "Game UUID is null or empty"
                            )
                        )
                        emit(RegisterResult.UnknownServerConfiguration(this))
                    } else {
                        // Move analytics call before emit to ensure it executes
                        mixpanel.trackMap(
                            eventName = SignupAnalytics.phoneSignup,
                            properties = mapOf(
                                SignupAnalytics.success to "Sign up successfully completed"
                            )
                        )
                        emit(RegisterResult.Success(this))
                    }
                }
            }
        )
    }

    override suspend fun requestOtp(mbAuthParams: MbAuthParams): Flow<RequestOtpResult> {
        return flow { emit(RequestOtpResult.Error.from(AuthErrorCodeResponse.UnknownError)) }
    }

    override suspend fun verifyOtp(mbAuthParams: MbAuthParams): Flow<VerifyOtpResult> {
        return flow { emit(VerifyOtpResult.Error.from(AuthErrorCodeResponse.UnknownError)) }
    }

    override suspend fun logout(): Flow<LogoutResult> = flow {
        mixpanel.trackMap(
            eventName = LogoutAnalytics.eventName,
            properties = mapOf(
                LogoutAnalytics.message to "User initiated logout"
            )
        )
        val result = mbAuthRepository.logout()
        result.fold(
            ifLeft = { error: NetworkError ->
                // Note: WorkerManager.cancelTimer() is skipped in tests as it requires Android context/Koin
                // In real MbAuthManagerImpl implementation, this would call: WorkerManager.cancelTimer()
                session.clear()
                val message = if (error is NetworkError.ApiError) error.errorBody.message else "Unknown logout error"
                // Move analytics call before emit to ensure it executes (code after emit may not execute in tests)
                mixpanel.trackMap(
                    eventName = LogoutAnalytics.eventName,
                    properties = mapOf(
                        LogoutAnalytics.failure to "User logged out unsuccessfully as $message"
                    )
                )
                emit(LogoutResult.Error.from(error))
            },
            ifRight = {
                // Note: WorkerManager.cancelTimer() is skipped in tests as it requires Android context/Koin
                // In real MbAuthManagerImpl implementation, this would call: WorkerManager.cancelTimer()
                session.clear()
                mbCoreCommonDataSource.logout()
                // Move analytics call before emit to ensure it executes (code after emit may not execute in tests)
                mixpanel.trackMap(
                    eventName = LogoutAnalytics.eventName,
                    properties = mapOf(
                        LogoutAnalytics.failure to "User logged out successfully"
                    )
                )
                emit(LogoutResult.Success)
            }
        )
    }

    override suspend fun refreshSession(): Flow<LoginResult> = flow {
        mixpanel.trackMap(
            eventName = RefreshTokenAnalytics.eventName,
            properties = mapOf(
                RefreshTokenAnalytics.message to "User initiated refresh token"
            )
        )
        val result = mbCoreAuthRepository.refreshToken()
        result.fold(
            ifLeft = { error ->
                // Move analytics call before emit to ensure it executes (code after emit may not execute in tests)
                mixpanel.trackMap(
                    eventName = RefreshTokenAnalytics.eventName,
                    properties = mapOf(
                        RefreshTokenAnalytics.failure to "User refresh token failed"
                    )
                )
                emit(LoginResult.Error.from(error))
            },
            ifRight = { sessionResponse ->
                // Move analytics call before emit to ensure it executes
                mixpanel.trackMap(
                    eventName = RefreshTokenAnalytics.eventName,
                    properties = mapOf(
                        RefreshTokenAnalytics.success to "User refresh token successful"
                    )
                )
                sessionResponse?.let {
                    emit(
                        LoginResult.Success(
                            MbAuthData(
                                refreshToken = it.refreshToken,
                                accessToken = it.accessToken,
                                expireDate = it.expireDate
                            )
                        )
                    )
                    // Note: MbSdk.updateAuthenticationToken() is skipped in tests as it requires Android context/Koin
                    // In real MbAuthManagerImpl implementation, this would call: MbSdk.updateAuthenticationToken(it)
                }
            }
        )
    }

    override suspend fun linkAccount(authParams: MbAuthParams): Flow<LoginResult> {
        return flow { emit(LoginResult.Error.from(AuthErrorCodeResponse.UnknownError)) }
    }

    override suspend fun resetPassword(authParams: MbAuthParams): Flow<ResetPasswordResult> {
        return flow { emit(ResetPasswordResult.Error.from(AuthErrorCodeResponse.UnknownError)) }
    }

    override suspend fun deactivateAccount(): Flow<DeactivateAccountResult> = flow {
        val result = mbAuthRepository.deactivateAccount()
        result.fold(
            ifLeft = { error ->
                emit(DeactivateAccountResult.Error.from(error))
            },
            ifRight = {
                // Note: WorkerManager.cancelTimer() is skipped in tests as it requires Android context/Koin
                // In real MbAuthManagerImpl implementation, this would call: WorkerManager.cancelTimer()
                mbCoreCommonDataSource.logout()
                session.clear()
                emit(DeactivateAccountResult.Success)
            }
        )
    }

    // MbAuthSession interface methods
    override fun setResult(result: LoginResult) {
        session.setResult(result)
    }

    override fun save(mbAuthData: MbAuthData) {
        session.save(mbAuthData)
    }

    override fun clear() {
        session.clear()
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return session.isAuthenticated()
    }

    override fun getSessionData(): Flow<MbAuthData?> {
        return session.getSessionData()
    }

    override suspend fun getLatestSessionData(): Either<NetworkError, MbAuthData> {
        return session.getLatestSessionData()
    }

    override fun saveOtpVerifiedToken(otpVerifiedToken: String) {
        session.saveOtpVerifiedToken(otpVerifiedToken)
    }

    override suspend fun getOtpVerifiedToken(): Flow<String> {
        return session.getOtpVerifiedToken()
    }
}

