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
import com.appmb.sdk.mbauth.network.MbAuthApi
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError

class MbAuthRemoteDataSource(
  val mbSdkAuthApi: MbAuthApi,
) : MbAuthDataSource {

  override suspend fun loginByPhone(mbLoginRequest: MbLoginByPhoneRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> {
    return mbSdkAuthApi.loginByPhone(mbLoginRequest)
  }

  override suspend fun loginBySocial(mbLoginSocialRequest: MbLoginSocialRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> {
    return mbSdkAuthApi.loginBySocial(mbLoginSocialRequest)
  }

  override suspend fun loginGuest(mbLoginGuestRequest: MbLoginGuestRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> {
    return mbSdkAuthApi.loginGuest(mbLoginGuestRequest)
  }

  override suspend fun register(
    mbRegisterRequest: MbRegisterRequest,
  ): Either<NetworkError, ResponseWrapper<RegisterDataModel>> {
    return mbSdkAuthApi.register(mbRegisterRequest)
  }

  override suspend fun linkSocialAccount(mbLinkSocialAccountRequest: MbLinkSocialAccountRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> {
    return mbSdkAuthApi.linkSocialAccount(mbLinkSocialAccountRequest)
  }

  override suspend fun linkPhoneAccount(mbRegisterRequest: MbRegisterRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>> {
    return mbSdkAuthApi.linkPhoneAccount(mbRegisterRequest)
  }

  override suspend fun logout(): Either<NetworkError, Unit> {
    return mbSdkAuthApi.logout()
  }

  override suspend fun requestOtp(mbRequestOtpRequest: MbRequestOtpRequest): Either<NetworkError, ResponseWrapper<RequestOtpData>> =
    mbSdkAuthApi.requestOtp(mbRequestOtpRequest)

  override suspend fun verifyOtp(mbVerifyOtpRequest: MbVerifyOtpRequest): Either<NetworkError, ResponseWrapper<VerifyOtpData>> =
    mbSdkAuthApi.verifyOtp(mbVerifyOtpRequest)

  override suspend fun resetPassword(mbResetPasswordRequest: MbResetPasswordRequest): Either<NetworkError, ResponseWrapper<ResetPasswordDataModel>> =
    mbSdkAuthApi.resetPassword(mbResetPasswordRequest)

  override suspend fun deactivateAccount(): Either<NetworkError, Unit> =
    mbSdkAuthApi.deactivateAccount()
}