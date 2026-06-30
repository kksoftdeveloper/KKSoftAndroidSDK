package com.appmb.sdk.mbauth.core.provider

import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.error.NetworkError

/**
 * Interface for authentication providers.
 * Provides methods to validate authentication parameters and perform authentication.
 */
internal interface MbAuthProvider {

  /**
   * Validates the provided authentication parameters.
   *
   * @param authParams The authentication parameters to validate.
   * @return `true` if the parameters are valid, `false` otherwise.
   */
  fun validate(authParams: MbAuthParams): Boolean

  fun validateLoginParams(authParams: MbAuthParams): Boolean {
    return true
  }

  fun validateRegisterParams(authParams: MbAuthParams): Boolean {
    return false
  }

  /**
   * Authenticates the user with the provided authentication parameters.
   *
   * @param authParams The authentication parameters to be used for authentication.
   * @return A Flow emitting the authentication result.
   */
  suspend fun login(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?>

  /**
   * Registers a new user with the provided authentication parameters.
   *
   * @param authParams The authentication parameters to be used for registration.
   * @return A Flow emitting the result of the registration operation.
   */
  suspend fun register(authParams: MbAuthParams): Either<NetworkError, RegisterDataModel?>

  /**
   * Link guest user to phone or social account
   *
   * @param authParams The authentication parameters to be used for link operation.
   * @return A Flow emitting the result of the link operation.
   */
  suspend fun linkAccount(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?>
}