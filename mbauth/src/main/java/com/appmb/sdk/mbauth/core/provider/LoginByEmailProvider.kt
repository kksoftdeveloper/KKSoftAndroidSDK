package com.appmb.sdk.mbauth.core.provider

import arrow.core.Either
import arrow.core.left
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.error.NetworkError

internal class LoginByEmailProvider() : MbAuthProvider {
  override fun validate(authParams: MbAuthParams): Boolean {
    return (authParams.email.isNullOrEmpty() || authParams.password.isNullOrEmpty()).not()
  }

  override suspend fun login(authParams: MbAuthParams): Either<NetworkError, LoginDataModel> {
    TODO("Not yet implemented")
  }

  override suspend fun register(authParams: MbAuthParams): Either<NetworkError, RegisterDataModel> {
    return NetworkError.UnSupportOperationError("Not support this register method").left()
  }

  override suspend fun linkAccount(authParams: MbAuthParams): Either<NetworkError, LoginDataModel> {
    return NetworkError.UnSupportOperationError("Not support this register method").left()
  }
}