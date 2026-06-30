package com.appmb.sdk.mbcore.session

import arrow.core.Either
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.MbAuthData
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing authentication sessions.
 * Provides methods to set authentication results, save user information, clear sessions, and retrieve user information.
 */
interface MbAuthSession {

  /**
   * Sets the authentication result.
   *
   * @param result The authentication result to set.
   */
  fun setResult(result: LoginResult)

  /**
   * Saves the user information.
   *
   * @param mbAuthData The user information to save.
   */
  fun save(mbAuthData: MbAuthData)

  /**
   * Clears the authentication session.
   */
  fun clear()

  fun isAuthenticated(): Flow<Boolean>

  fun getSessionData(): Flow<MbAuthData?>

  suspend fun getLatestSessionData(): Either<NetworkError, MbAuthData>

  /**
   * Saves the OTP verified token.
   *
   * @param otpVerifiedToken The OTP verified token to save.
   */
  fun saveOtpVerifiedToken(otpVerifiedToken: String)

  /**
   * Retrieves the OTP verified token.
   */
  suspend fun getOtpVerifiedToken(): Flow<String>
}