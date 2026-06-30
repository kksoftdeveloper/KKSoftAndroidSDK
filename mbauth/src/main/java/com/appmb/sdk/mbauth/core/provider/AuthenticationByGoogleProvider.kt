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

internal class AuthenticationByGoogleProvider(
  val repository: MbAuthRepository,
) : MbAuthProvider {

  override fun validate(authParams: MbAuthParams): Boolean {
    return authParams.context != null
  }

  override suspend fun login(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    val googleToken = authParams.googleAccount?.idToken
    if (googleToken.isNullOrEmpty()) {
      Log.e(MbConstant.TAG, "Google ID Token is null or empty in login.")
      return NetworkError.DataNullError.left()
    }
    return repository.loginByGoogle(googleToken)
  }

  override suspend fun register(authParams: MbAuthParams): Either<NetworkError, RegisterDataModel> {
    return NetworkError.UnSupportOperationError("Not support this register method").left()
  }

  override suspend fun linkAccount(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    val googleToken = authParams.googleAccount?.idToken
    if (googleToken.isNullOrEmpty()) {
      Log.e(MbConstant.TAG, "Google ID Token is null or empty in linkAccount.")
      return NetworkError.DataNullError.left()
    }
    return repository.linkSocialAccount(authParams, googleToken)
  }
}
