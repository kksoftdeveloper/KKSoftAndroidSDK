package com.appmb.sdk.mbauth.core.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.domain.MbServerRepository
import com.appmb.sdk.mbauth.event.LogoutAnalytics
import com.appmb.sdk.mbauth.event.OTPAnalytics
import com.appmb.sdk.mbauth.event.RefreshTokenAnalytics
import com.appmb.sdk.mbauth.event.SignupAnalytics
import com.appmb.sdk.mbauth.event.toAnalyticsEventName
import com.appmb.sdk.mbauth.model.LogoutResult
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
import com.appmb.sdk.mbauth.tracking.AuthTracking
import com.appmb.sdk.mbauth.worker.WorkerManager
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.domain.auth.MbCoreAuthRepository
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.DeactivateAccountResult
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the MbAuthManager interface.
 *
 * This class provides methods for user authentication, registration, and session management.
 */
internal class MbAuthManagerImpl(
  val mbAuthProviderFactory: MbAuthProviderFactory,
  private val mixpanel: AnalyticsProvider
) : MbAuthManager {

  // Inject Auth Repository
  private val mbAuthRepository = MbSdk.getKoin().get<MbAuthRepository>()

  // Inject Core Auth repository
  private val mbCoreAuthRepository = MbSdk.getKoin().get<MbCoreAuthRepository>()

  private val mbServerRepository = MbSdk.getKoin().get<MbServerRepository>()

  private val mbCoreCommonDataSource = MbSdk.getKoin().get<MbCoreCommonDataSource>()

  /**
   * Retrieves the authentication provider for the specified login type.
   *
   * @param type The type of login for which the provider is needed.
   * @return The authentication provider for the specified login type.
   */
  override fun getProvider(type: LoginType): MbAuthProvider {
    return mbAuthProviderFactory.getProvider(type)
  }

  /**
   * Validates the provided authentication parameters.
   *
   * @param authParams The authentication parameters to be validated.
   * @return A boolean indicating whether the validation was successful.
   */
  fun validate(authParams: MbAuthParams): Boolean {
    return authParams.type?.let { getProvider(it).validate(authParams) } ?: false
  }

  /**
   * Login the user with the provided authentication parameters.
   *
   * This method first checks if the MbAuth configuration is valid. If not, it emits an error.
   * It then validates the authentication parameters. If they are invalid, it emits an error.
   * Finally, it performs the authentication and emits the result.
   *
   * @param authParams The authentication parameters to be used for authentication.
   * @return A Flow emitting the authentication result.
   */
  override suspend fun login(authParams: MbAuthParams): Flow<LoginResult> = flow {
    if (validateAuth(authParams)) {
      mixpanel.trackMap(
        eventName = authParams.toAnalyticsEventName(),
        properties = mapOf(
          "failure" to "Invalid parameters",
        )
      )
      return@flow
    }

    if (authParams.type == null) {
      mixpanel.trackMap(
        eventName = authParams.toAnalyticsEventName(),
        properties = mapOf(
          "failure" to "Could not find login provider",
        )
      )
      return@flow
    }

    val result = getProvider(authParams.type!!).login(authParams)
    val trackingMethod = authParams.type?.name?.lowercase() ?: "unknown"
    result.fold(
      ifLeft = { error ->
        val message =
          if (error is NetworkError.ApiError) error.errorBody.message else "Error occurred during login"
        mixpanel.trackMap(
          eventName = authParams.toAnalyticsEventName(),
          properties = mapOf("failure" to message)
        )
        AuthTracking.logLoginFailure(trackingMethod, error)
        emit(LoginResult.Error.from(error))
        getSession().clear()
      },
      ifRight = {
        it?.toEntity()?.apply {
          getSession().save(this)
          MbSdk.getAuthNetwork().apply {
            saveToken(
              accessToken = accessToken.orEmpty(),
              refreshToken = refreshToken.orEmpty()
            )
          }
          if (this.gameUuid.isNullOrEmpty()) {
            mixpanel.trackMap(
              eventName = authParams.toAnalyticsEventName(),
              properties = mapOf(
                "failure" to "Game UUID is null or empty",
              )
            )
//            emit(LoginResult.UnknownServerConfiguration(this))
          } else {
            AuthTracking.logLoginSuccess(trackingMethod, this)
            mixpanel.trackMap(
              eventName = authParams.toAnalyticsEventName(),
              properties = mapOf(
                "success" to "${authParams.toAnalyticsEventName()} successfully logged in",
              )
            )
          }
          emit(LoginResult.Success(this))
        }
      }
    )
  }

  private fun validateCommonError(authParams: MbAuthParams): Boolean {
    return MbSdk.getConfig().isValid()
  }

  /**
   * Registers a new user with the provided authentication parameters.
   *
   * This method first validates the authentication parameters. If they are invalid, it emits an error.
   * It then performs the registration and emits the result.
   *
   * @param authParams The authentication parameters to be used for registration.
   * @return A Flow emitting the result of the registration operation.
   */
  override suspend fun register(authParams: MbAuthParams): Flow<RegisterResult> = flow {
    if (validateRegister(authParams)) {
      mixpanel.trackMap(
        eventName = SignupAnalytics.phoneSignup,
        properties = mapOf(
          SignupAnalytics.failure to "Invalid parameters",
        )
      )
      return@flow
    }

    if (authParams.type == null) {
      mixpanel.trackMap(
        eventName = SignupAnalytics.phoneSignup,
        properties = mapOf(
          SignupAnalytics.failure to "Could not find Auth provider",
        )
      )
      return@flow
    }

    val result = getProvider(authParams.type!!).register(authParams)
    val trackingMethod = authParams.type?.name?.lowercase() ?: "unknown"

    result.fold(
      ifLeft = {
        mixpanel.trackMap(
          eventName = SignupAnalytics.phoneSignup,
          properties = mapOf(
            SignupAnalytics.failure to "Signup failed",
          )
        )
        getSession().clear()
        emit(RegisterResult.Error.from(it))
      },
      ifRight = { data ->
        getSession().clear()
        data?.toEntity()?.apply {
          val mbAuthData = this
          getSession().save(this)
          MbSdk.getAuthNetwork().apply {
            invalidateAuthTokens()
            saveToken(
              accessToken = mbAuthData.accessToken.orEmpty(),
              refreshToken = mbAuthData.refreshToken.orEmpty()
            )
          }
          if (this.gameUuid.isNullOrEmpty()) {
            mixpanel.trackMap(
              eventName = SignupAnalytics.phoneSignup,
              properties = mapOf(
                SignupAnalytics.success to "Game UUID is null or empty"
              )
            )
//            emit(RegisterResult.UnknownServerConfiguration(this))
          } else {
            AuthTracking.logRegisterSuccess(trackingMethod, this)
            mixpanel.trackMap(
              eventName = SignupAnalytics.phoneSignup,
              properties = mapOf(
                SignupAnalytics.success to "Sign up successfully completed",
              )
            )
//            emit(RegisterResult.Success(this))
          }
          emit(RegisterResult.Success(this))
        }
      }
    )
  }

  override suspend fun requestOtp(mbAuthParams: MbAuthParams): Flow<RequestOtpResult> = flow {
    mixpanel.trackMap(
      eventName = OTPAnalytics.requestOTP,
      properties = mapOf(
        OTPAnalytics.message to "User initiates OTP request"
      )
    )
    if (mbAuthParams.phone.isNullOrEmpty()) {
      mixpanel.trackMap(
        eventName = OTPAnalytics.requestOTP,
        properties = mapOf(
          OTPAnalytics.failure to "User's phone number is null or empty",
        )
      )
      emit(RequestOtpResult.Error.from(AuthErrorCodeResponse.InvalidPhoneError))
      return@flow
    }
    val result = mbAuthRepository.requestOtp(mbAuthParams)
    result.fold(
      ifLeft = { error ->
        mixpanel.trackMap(
          eventName = OTPAnalytics.requestOTP,
          properties = mapOf(
            OTPAnalytics.failure to "User fails to request OTP",
          )
        )
        emit(RequestOtpResult.Error.from(error))
      },
      ifRight = { data ->
        if (data?.otpSent == true) {
          mixpanel.trackMap(
            eventName = OTPAnalytics.requestOTP,
            properties = mapOf(
              OTPAnalytics.success to "User requests OTP successfully",
            )
          )
          emit(RequestOtpResult.Success(data))
        } else {
          mixpanel.trackMap(
            eventName = OTPAnalytics.requestOTP,
            properties = mapOf(
              OTPAnalytics.success to "User fails to requests OTP with false otpSent flag",
            )
          )
          emit(RequestOtpResult.Error.from(AuthErrorCodeResponse.OTPError))
        }
      }
    )
  }

  override suspend fun verifyOtp(mbAuthParams: MbAuthParams): Flow<VerifyOtpResult> = flow {
    mixpanel.trackMap(
      eventName = OTPAnalytics.verifyOTP,
      properties = mapOf(
        OTPAnalytics.message to "User initiates OTP verification"
      )
    )
    if (mbAuthParams.phone.isNullOrEmpty() || mbAuthParams.otp.isNullOrEmpty()) {
      mixpanel.trackMap(
        eventName = OTPAnalytics.verifyOTP,
        properties = mapOf(
          OTPAnalytics.failure to "User's phone number or OTP is null or empty"
        )
      )
      emit(VerifyOtpResult.Error.from(AuthErrorCodeResponse.InvalidPhoneError))
      return@flow
    }
    val result = mbAuthRepository.verifyOtp(mbAuthParams)
    result.fold(
      ifLeft = { error ->
        mixpanel.trackMap(
          eventName = OTPAnalytics.verifyOTP,
          properties = mapOf(
            OTPAnalytics.failure to "User fails to verify OTP"
          )
        )
        emit(VerifyOtpResult.Error.from(error))
      },
      ifRight = { data ->
        mixpanel.trackMap(
          eventName = OTPAnalytics.verifyOTP,
          properties = mapOf(
            OTPAnalytics.success to "User fails to verify OTP"
          )
        )
        data?.otpVerifiedToken?.takeIf { it.isNotEmpty() }?.let { token ->
          getSession().saveOtpVerifiedToken(token)
          emit(VerifyOtpResult.Success(data))
        } ?: emit(VerifyOtpResult.Error.from(AuthErrorCodeResponse.OTPInvalid))
      }
    )
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
        WorkerManager.cancelTimer()
        getSession().clear()
        val message = if (error is NetworkError.ApiError) error.errorBody.message else "Unknown logout error"
        emit(LogoutResult.Error.from(error))
        mixpanel.trackMap(
          eventName = LogoutAnalytics.eventName,
          properties = mapOf(
            LogoutAnalytics.failure to "User logged out unsuccessfully as $message"
          )
        )
      },
      ifRight = {
        WorkerManager.cancelTimer()
        getSession().clear()
        mbCoreCommonDataSource.logout()
        emit(LogoutResult.Success)
        mixpanel.trackMap(
          eventName = LogoutAnalytics.eventName,
          properties = mapOf(
            LogoutAnalytics.failure to "User logged out successfully"
          )
        )
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
      ifLeft = {
        emit(LoginResult.Error.from(it))
        mixpanel.trackMap(
          eventName = RefreshTokenAnalytics.eventName,
          properties = mapOf(
            RefreshTokenAnalytics.failure to "User refresh token failed",
          )
        )

      }, ifRight = { sessionResponse ->
        mixpanel.trackMap(
          eventName = RefreshTokenAnalytics.eventName,
          properties = mapOf(
            RefreshTokenAnalytics.success to "User refresh token successful",
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
          MbSdk.updateAuthenticationToken(it)
        }
      }
    )
  }

  override suspend fun linkAccount(authParams: MbAuthParams): Flow<LoginResult> = flow {

    if (authParams.type == null) {
      mixpanel.trackMap(
        eventName = authParams.toAnalyticsEventName(),
        properties = mapOf(
          "failure" to "Could not find login provider",
        )
      )
      return@flow
    }
    val result = getProvider(authParams.type!!).linkAccount(authParams)
    result.fold(
      ifLeft = {
        getSession().clear()
        emit(LoginResult.Error.from(it))
      },
      ifRight = { it ->
        it?.toEntity()?.apply {
          val mbAuthData = this
          getSession().save(this)
          mbCoreCommonDataSource.saveIsGuestUser(isGuest = false)
          MbSdk.getAuthNetwork().apply {
            invalidateAuthTokens()
            saveToken(
              accessToken = mbAuthData.accessToken.orEmpty(),
              refreshToken = mbAuthData.refreshToken.orEmpty()
            )
          }
          emit(LoginResult.Success(this))
        }
      }
    )
  }

  override suspend fun resetPassword(authParams: MbAuthParams): Flow<ResetPasswordResult> = flow {
    if (authParams.phone.isNullOrEmpty() || authParams.password.isNullOrEmpty()) emit(
      ResetPasswordResult.Error.from(AuthErrorCodeResponse.InvalidPhoneError)
    )
    val result = mbAuthRepository.resetPassword(authParams)
    result.fold(
      ifLeft = { error ->
        emit(ResetPasswordResult.Error.from(error))
      },
      ifRight = {
        emit(ResetPasswordResult.Success)
      }
    )
  }

  override suspend fun deactivateAccount(): Flow<DeactivateAccountResult> = flow {
    val result = mbAuthRepository.deactivateAccount()
    result.fold(
      ifLeft = {
        emit(DeactivateAccountResult.Error.from(it))
      },
      ifRight = {
        WorkerManager.cancelTimer()
        mbCoreCommonDataSource.logout()
//        mbCoreCommonDataSource.clear()
        getSession().clear()
        emit(DeactivateAccountResult.Success)
      }
    )
  }

  override fun setResult(result: LoginResult) {
    getSession().setResult(result)
  }

  override fun saveOtpVerifiedToken(otpVerifiedToken: String) {
    getSession().saveOtpVerifiedToken(otpVerifiedToken)
  }

  override suspend fun getOtpVerifiedToken(): Flow<String> {
    return getSession().getOtpVerifiedToken()
  }

  override fun save(mbAuthData: MbAuthData) {
    getSession().save(mbAuthData)
  }

  override fun clear() {
    getSession().clear()
  }

  override fun isAuthenticated(): Flow<Boolean> {
    return getSession().isAuthenticated()
  }

  override fun getSessionData(): Flow<MbAuthData?> {
    return getSession().getSessionData()
  }

  override suspend fun getLatestSessionData(): Either<NetworkError, MbAuthData> {
    val serverId = mbCoreCommonDataSource.getServerId()
//    if (serverId.isNullOrEmpty()) {
//      mixpanel.trackMap(
//        eventName = "Get Latest Session Data",
//        properties = mapOf(
//          "failure" to "Server ID is null or empty"
//        )
//      )
//      return NetworkError.DataNullError.left()
//    }

    return mbServerRepository.getGameUuid(serverId = serverId).fold(
      ifLeft = { error ->
        error.left()
        if (error is NetworkError.ApiError && error.statusCode != 401) {
          mixpanel.trackMap(
            eventName = "Get Latest Session Data",
            properties = mapOf(
              "failure" to "Failed to get game UUID: ${error.errorBody.message}"
            )
          )
          val session = getSession().getSessionData().firstOrNull()
          if (session != null) {
            session.right()
          } else {
            mixpanel.trackMap(
              eventName = "Get Latest Session Data",
              properties = mapOf(
                "failure" to "Session data is null"
              )
            )
            NetworkError.DataNullError.left()
          }
        } else {
          mixpanel.trackMap(
            eventName = "Get Latest Session Data",
            properties = mapOf(
              "failure" to "Failed to get game UUID: Unknown error"
            )
          )
          error.left()
        }
      },
      ifRight = { data ->
        val session = getSession().getSessionData().firstOrNull()
        
        if (session == null) {
          mixpanel.trackMap(
            eventName = "Get Latest Session Data",
            properties = mapOf(
              "failure" to "Session data is null"
            )
          )
          return@fold NetworkError.DataNullError.left()
        }

        val updated = session.copy(
          gameUuid = data?.gameUuid,
          serverId = serverId
        )
        
        getSession().save(updated)
        
        mixpanel.trackMap(
          eventName = "Get Latest Session Data",
          properties = mapOf(
            "success" to "Session data updated successfully",
            "gameUuid" to (data?.gameUuid ?: "null"),
            "serverId" to serverId
          )
        )
        
        updated.right()
      }
    )
  }

  /**
   * Validates the authentication parameters and emits an error if they are invalid.
   *
   * This method first checks if the MbAuth configuration is valid. If not, it emits an error.
   * It then validates the authentication parameters using the appropriate provider. If they are invalid, it emits an error.
   *
   * @param authParams The authentication parameters to be validated.
   * @return A boolean indicating whether the validation failed (true) or succeeded (false).
   */
  private suspend fun FlowCollector<LoginResult>.validateAuth(
    authParams: MbAuthParams,
  ): Boolean {
    if (!MbSdk.getConfig().isValid()) {
      emit(LoginResult.Error.from(AuthErrorCodeResponse.SDKNotInitialized))
      return true
    }
    if (authParams.type == null)
      return true
    if (getProvider(authParams.type!!).validate(authParams).not()) {
      emit(LoginResult.Error.from(AuthErrorCodeResponse.MatchError))
      return true
    }
    return false
  }

  /**
   * Validates the authentication parameters and emits an error if they are invalid.
   *
   * This method first checks if the MbAuth configuration is valid. If not, it emits an error.
   * It then validates the authentication parameters using the appropriate provider. If they are invalid, it emits an error.
   *
   * @param authParams The authentication parameters to be validated.
   * @return A boolean indicating whether the validation failed (true) or succeeded (false).
   */
  private suspend fun FlowCollector<RegisterResult>.validateRegister(authParams: MbAuthParams): Boolean {
    if (!MbSdk.getConfig().isValid()) {
      emit(RegisterResult.Error.from(AuthErrorCodeResponse.MatchError))
      return true
    }
    if (authParams.type == null) {
      return true
    }
    if (!getProvider(authParams.type!!).validate(authParams)) {
      emit(RegisterResult.Error.from(AuthErrorCodeResponse.MatchError))
      return true
    }
    return false
  }
}