package com.appmb.sdk.mbauth.core.auth

import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.core.provider.MbAuthProvider
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
import com.appmb.sdk.mbcore.model.DeactivateAccountResult
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbauth.model.LogoutResult
import com.appmb.sdk.mbcore.session.MbAuthSession
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing authentication operations.
 * Extends the MbAuthProvider interface to provide additional methods for OTP requests, verification, logout, and session refresh.
 */
internal interface MbAuthManager : MbAuthSession {

  /**
   * Retrieves the authentication provider for the specified login type.
   *
   * @param type The type of login for which the provider is needed.
   * @return The authentication provider for the specified login type.
   */
  fun getProvider(type: LoginType): MbAuthProvider

  /**
   * Initiates the login process with the provided authentication parameters.
   *
   * @param authParams The authentication parameters containing the phone number and password.
   * @return A Flow emitting the result of the login operation.
   */
  suspend fun login(authParams: MbAuthParams): Flow<LoginResult>

  /**
   * Initiates the registration process with the provided authentication parameters.
   *
   * @param authParams The authentication parameters containing the necessary registration details.
   * @return A Flow emitting the result of the registration operation.
   */
  suspend fun register(authParams: MbAuthParams): Flow<RegisterResult>

  /**
   * Requests an OTP for the provided phone number.
   *
   * @param mbAuthParams The params to request the OTP for.
   * @return A Flow emitting a result of the OTP request operation.
   */
  suspend fun requestOtp(mbAuthParams: MbAuthParams): Flow<RequestOtpResult>

  /**
   * Verifies the OTP for the provided phone number.
   *
   * @param mbAuthParams The params to verify OTP
   * @return A Flow emitting a result of the OTP verification operation.
   */
  suspend fun verifyOtp(mbAuthParams: MbAuthParams): Flow<VerifyOtpResult>

  /**
   * Logs out the user by clearing the session.
   *
   * @return A Flow emitting a boolean indicating whether the logout operation was successful.
   */
  suspend fun logout(): Flow<LogoutResult>

  /**
   * Refreshes the authentication session.
   *
   * @return A Flow emitting the result of the session refresh operation.
   */
  suspend fun refreshSession(): Flow<LoginResult>

  /**
   * Link guest user to Social or Phone account
   *
   * @param authParams The authentication parameters containing the necessary for link account operation.
   * @return A Flow emitting the result of the link account operation.
   */
  suspend fun linkAccount(authParams: MbAuthParams): Flow<LoginResult>

  /**
   * Reset password
   *
   * @param authParams The authentication parameters containing the necessary for reset password
   * @return A Flow emitting the result of the reset password operation.
   */
  suspend fun resetPassword(authParams: MbAuthParams): Flow<ResetPasswordResult>

  /**
   * Deactivate account
   * @return A Flow emitting the result of the deactivate account operation.
   */
  suspend fun deactivateAccount(): Flow<DeactivateAccountResult>
}