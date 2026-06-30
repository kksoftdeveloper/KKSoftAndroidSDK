package com.appmb.sdk.mbauth.data.datasource

import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.request.MbLinkSocialAccountRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginByPhoneRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginGuestRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginSocialRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRegisterRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRequestOtpRequest
import com.appmb.sdk.mbauth.data.dto.request.MbResetPasswordRequest
import com.appmb.sdk.mbauth.data.dto.request.MbVerifyOtpRequest
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.data.dto.response.RequestOtpData
import com.appmb.sdk.mbauth.data.dto.response.ResetPasswordDataModel
import com.appmb.sdk.mbauth.data.dto.response.VerifyOtpData
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError

/**
 * Interface for authentication data source.
 * Provides methods for login, registration, and logout operations.
 */
interface MbAuthDataSource {

  /**
   * Logs in a user with the provided email and password.
   *
   * @param mbLoginRequest The phone number and password of the user.
   * @return A Flow emitting the result of the login operation.
   */
  suspend fun loginByPhone(mbLoginRequest: MbLoginByPhoneRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  suspend fun loginBySocial(
    mbLoginSocialRequest: MbLoginSocialRequest,
  ): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  /**
   * Logs in a user guest.
   *
   * @param mbLoginGuestRequest The guest user information of the user.
   * @return A Flow emitting the result of the login operation.
   */
  suspend fun loginGuest(mbLoginGuestRequest: MbLoginGuestRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  /**
   * Registers a new user with the provided email and password.
   *
   * @param mbRegisterRequest The phone number and password of the user.
   * @return A string indicating the result of the registration operation.
   */
  suspend fun register(
    mbRegisterRequest: MbRegisterRequest,
  ): Either<NetworkError, ResponseWrapper<RegisterDataModel>>

  /**
   * Link current user with social account.
   *
   * @param mbLinkSocialAccountRequest The social account data.
   * @return  A flow which emits the result of the link account operation.
   */
  suspend fun linkSocialAccount(
    mbLinkSocialAccountRequest: MbLinkSocialAccountRequest,
  ): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  /**
   * Link current user with phone number.
   *
   * @param mbRegisterRequest The phone number and password of the user.
   * @return A string indicating the result of the registration operation.
   */
  suspend fun linkPhoneAccount(
    mbRegisterRequest: MbRegisterRequest,
  ): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  /**
   * Logs out the current user.
   *
   * @return A boolean indicating whether the logout operation was successful.
   */
  suspend fun logout(): Either<NetworkError, Unit>

  /**
   * Send a request to get OTP
   *
   * @param mbRequestOtpRequest The request OTP params.
   * @return A flow which emits the result of the request OTP operation.
   */
  suspend fun requestOtp(
    mbRequestOtpRequest: MbRequestOtpRequest,
  ): Either<NetworkError, ResponseWrapper<RequestOtpData>>

  /**
   * Send a request to verify OTP.
   *
   * @param mbVerifyOtpRequest The verify OTP params.
   * @return A flow which emits the result of the verify OTP operation.
   */
  suspend fun verifyOtp(
    mbVerifyOtpRequest: MbVerifyOtpRequest,
  ): Either<NetworkError, ResponseWrapper<VerifyOtpData>>

  /**
   * Reset password.
   *
   * @param mbResetPasswordRequest The reset password params.
   * @return A flow which emits the result of the reset password operation.
   */
  suspend fun resetPassword(
    mbResetPasswordRequest: MbResetPasswordRequest,
  ): Either<NetworkError, ResponseWrapper<ResetPasswordDataModel>>

  /**
   * Deactivate account
   */
  suspend fun deactivateAccount(): Either<NetworkError, Unit>
}