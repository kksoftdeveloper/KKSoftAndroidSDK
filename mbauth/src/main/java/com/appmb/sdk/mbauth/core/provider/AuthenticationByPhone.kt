package com.appmb.sdk.mbauth.core.provider

import android.util.Log
import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.event.LoginAnalytics
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.config.MbConstant
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider

/**
 * Provider for user authentication using phone number.
 *
 * @property authRepository The repository used for authentication operations.
 */
internal class AuthenticationByPhone(
  var authRepository: MbAuthRepository
) : MbAuthProvider {

  /**
   * Validates the provided authentication parameters.
   *
   * @param authParams The authentication parameters to validate.
   * @return `true` if the parameters are valid, `false` otherwise.
   */
  override fun validate(authParams: MbAuthParams): Boolean {
    return (authParams.phone.isNullOrEmpty() || authParams.password.isNullOrEmpty()).not()
  }

  override fun validateRegisterParams(authParams: MbAuthParams): Boolean {
    return (authParams.phone.isNullOrEmpty() || authParams.password.isNullOrEmpty()).not()
  }

  /**
   * Authenticates the user with the provided authentication parameters.
   *
   * @param authParams The authentication parameters to be used for authentication.
   * @return A Flow emitting the authentication result.
   */
  override suspend fun login(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {

    Log.d(
      MbConstant.TAG,
      "authenticate: phone: ${authParams.phone} password: ${authParams.password}"
    )
    return authRepository.loginByPhone(authParams)
  }

  /**
   * Registers a new user with the provided authentication parameters.
   *
   * @param authParams The authentication parameters to be used for registration.
   * @return A Flow emitting the result of the registration operation.
   */
  override suspend fun register(authParams: MbAuthParams): Either<NetworkError, RegisterDataModel?> {
    Log.d(MbConstant.TAG, "register: phone: ${authParams.phone} password: ${authParams.password}")
    return authRepository.register(authParams)
  }

  override suspend fun linkAccount(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    return authRepository.linkPhoneAccount(authParams)
  }
}