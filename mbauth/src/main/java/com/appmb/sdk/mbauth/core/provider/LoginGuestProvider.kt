package com.appmb.sdk.mbauth.core.provider

import android.util.Log
import arrow.core.Either
import arrow.core.left
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.config.MbConstant
import com.appmb.sdk.mbcore.error.NetworkError

/**
 * Provider for user guest login.
 *
 * @property authRepository The repository used for authentication operations.
 */
internal class LoginGuestProvider(
  var authRepository: MbAuthRepository,
) : MbAuthProvider {

  /**
   * Validates the provided authentication parameters.
   *
   * @param authParams The authentication parameters to validate.
   * @return `true` if the parameters are valid, `false` otherwise.
   */
  override fun validate(authParams: MbAuthParams): Boolean {
    return true
  }

  /**
   * Authenticates the user with the provided authentication parameters.
   *
   * @param authParams The authentication parameters to be used for authentication.
   * @return A Flow emitting the authentication result.
   */
  override suspend fun login(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    Log.d(MbConstant.TAG, "authenticate: guest info: $authParams")
    return authRepository.loginGuest(authParams)
  }

  /**
   * Registers a new user with the provided authentication parameters.
   *
   * @param authParams The authentication parameters to be used for registration.
   * @return A Flow emitting the result of the registration operation.
   */
  override suspend fun register(authParams: MbAuthParams): Either<NetworkError, RegisterDataModel?> {
    Log.d(MbConstant.TAG, "register: phone: ${authParams.phone} password: ${authParams.password}")
    // Attempt to log in with guest.
    // So return NoSupport Error here
    return NetworkError.UnSupportOperationError("Not support this register method").left()
  }

  override suspend fun linkAccount(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    return NetworkError.UnSupportOperationError("Not support this register method").left()
  }
}