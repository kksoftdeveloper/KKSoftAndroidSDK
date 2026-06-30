package com.appmb.sdk.mbauth.domain

import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.data.dto.response.RequestOtpData
import com.appmb.sdk.mbauth.data.dto.response.ResetPasswordDataModel
import com.appmb.sdk.mbauth.data.dto.response.VerifyOtpData
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.error.NetworkError

/**
 * Interface for authentication repository.
 * Provides methods for login, registration, and logout operations.
 */
interface MbAuthRepository {

  /**
   * Logs in a user with the provided email and password.
   *
   * @param authParams authParams The authentication parameters to be used for login.
   * @return A Flow emitting the result of the login operation.
   */
  suspend fun loginByPhone(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?>

  suspend fun loginByGoogle(
    socialToken: String,
  ): Either<NetworkError, LoginDataModel?>

  suspend fun loginByFacebook(
    socialToken: String,
  ): Either<NetworkError, LoginDataModel?>

  /**
   * Logs in a user guest without any information.
   *
   * @param authParams authParams The authentication parameters to be used for login.
   * @return A Flow emitting the result of the login operation.
   */
  suspend fun loginGuest(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?>

  /**
   * Registers a new user with the provided email and password.
   *
   * @param phone The phone number of the new user.
   * @param password The password of the new user.
   * @return A string indicating the result of the registration operation.
   */
  suspend fun register(authParams: MbAuthParams): Either<NetworkError, RegisterDataModel?>

  /**
   * Link current user with social account
   *
   * @param mbAuthParams [MbAuthParams] object containing social account details
   */
  suspend fun linkSocialAccount(
    mbAuthParams: MbAuthParams,
    socialToken: String,
  ): Either<NetworkError, LoginDataModel?>

  /**
   * Link current user with phone number
   *
   * @param mbAuthParams [MbAuthParams] object containing phone number details
   */
  suspend fun linkPhoneAccount(mbAuthParams: MbAuthParams): Either<NetworkError, LoginDataModel?>

  /**
   * Logs out the current user.
   *
   * @return A boolean indicating whether the logout operation was successful.
   */
  suspend fun logout(): Either<NetworkError, Unit>

  /**
   * Requests an OTP code to be sent to the user's phone.
   *
   * @return A flow emitting the result of the request.
   */
  suspend fun requestOtp(mbAuthParams: MbAuthParams): Either<NetworkError, RequestOtpData?>

  /**
   * Verifies the OTP code sent to the user's phone.
   *
   * @return A flow emitting the result of the verification.
   */
  suspend fun verifyOtp(mbAuthParams: MbAuthParams): Either<NetworkError, VerifyOtpData?>

  /**
   * Reset password
   *
   * @param mbAuthParams [MbAuthParams] object containing phone number details
   * @return A flow emitting the result of the reset password operation.
   */
  suspend fun resetPassword(mbAuthParams: MbAuthParams): Either<NetworkError, ResetPasswordDataModel?>

  /**
   * Deactivate account
   */
  suspend fun deactivateAccount(): Either<NetworkError, Unit>
}