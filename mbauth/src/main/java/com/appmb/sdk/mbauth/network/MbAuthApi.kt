package com.appmb.sdk.mbauth.network

import arrow.core.Either
import com.appmb.sdk.mbauth.data.dto.request.MbGetListServersRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLinkSocialAccountRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginByPhoneRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginGuestRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginSocialRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRegisterRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRequestOtpRequest
import com.appmb.sdk.mbauth.data.dto.request.MbResetPasswordRequest
import com.appmb.sdk.mbauth.data.dto.request.MbVerifyOtpRequest
import com.appmb.sdk.mbauth.data.dto.response.GetGameUuidData
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.data.dto.response.RequestOtpData
import com.appmb.sdk.mbauth.data.dto.response.ResetPasswordDataModel
import com.appmb.sdk.mbauth.data.dto.response.VerifyOtpData
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError

interface MbAuthApi {

  suspend fun loginByPhone(request: MbLoginByPhoneRequest?): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  suspend fun loginGuest(request: MbLoginGuestRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  suspend fun loginBySocial(request: MbLoginSocialRequest?): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  suspend fun register(request: MbRegisterRequest?): Either<NetworkError, ResponseWrapper<RegisterDataModel>>

  suspend fun logout(): Either<NetworkError, Unit>

  suspend fun linkSocialAccount(request: MbLinkSocialAccountRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  suspend fun linkPhoneAccount(request: MbRegisterRequest): Either<NetworkError, ResponseWrapper<LoginDataModel>>

  suspend fun getGameUuid(
    gameId: String,
    serverId: String?,
  ): Either<NetworkError, ResponseWrapper<GetGameUuidData>>

  suspend fun requestOtp(request: MbRequestOtpRequest): Either<NetworkError, ResponseWrapper<RequestOtpData>>

  suspend fun verifyOtp(request: MbVerifyOtpRequest): Either<NetworkError, ResponseWrapper<VerifyOtpData>>

  suspend fun resetPassword(request: MbResetPasswordRequest): Either<NetworkError, ResponseWrapper<ResetPasswordDataModel>>

  suspend fun deactivateAccount(): Either<NetworkError, Unit>

  suspend fun getListServers(
    gameId: String,
  ): Either<NetworkError, ResponseWrapper<List<MbServer>>>

  companion object {
    const val LOGIN_PATH = "/sdk/api/v1/auth/login"
    const val REGISTER_PATH = "/sdk/api/v2/auth/register"
    const val LOGOUT_PATH = "/sdk/api/v1/users/logout"
    const val LINK_ACCOUNT_PATH = "/sdk/api/v1/auth/link-account"
    const val UPDATE_SERVER_ID_PATH =
      "/sdk/api/v1/characters/me?gameId={gameId}&serverId={serverId}"
    const val REQUEST_OTP_PATH = "sdk/api/v1/auth/send-otp"
    const val VERIFY_OTP_PATH = "/sdk/api/v1/auth/verify-otp"
    const val RESET_PASSWORD_PATH = "/sdk/api/v1/auth/reset-password"
    const val DEACTIVATE_ACCOUNT_PATH = "/sdk/api/v1/users/deactivate"
    const val GET_LIST_SERVERS_PATH = "/sdk/api/v1/games/{gameId}/servers"
  }
}

/***
 *     func getGameServers(gameId: Int) -> AnyPublisher<GameServerInfoServerResponse, any Error> {
 *         execute(
 *             endpoint: "/sdk/api/v1/games/\(gameId)/servers",
 *             method: "GET",
 *             header: nil,
 *             body: nil
 *         )
 *     }
 * */