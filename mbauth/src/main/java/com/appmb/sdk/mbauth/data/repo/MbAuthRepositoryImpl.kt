package com.appmb.sdk.mbauth.data.repo

import android.os.Build
import android.util.Log
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.data.datasource.MbAuthDataSource
import com.appmb.sdk.mbauth.data.datasource.MbCommonDataSource
import com.appmb.sdk.mbauth.data.dto.request.MbLinkSocialAccountRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginByPhoneRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginGuestRequest
import com.appmb.sdk.mbauth.data.dto.request.MbLoginSocialRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRegisterRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRequestOtpRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRegisterConsentRequest
import com.appmb.sdk.mbauth.data.dto.request.MbRegisterGuardianRequest
import com.appmb.sdk.mbauth.data.dto.request.MbResetPasswordRequest
import com.appmb.sdk.mbauth.data.dto.request.MbVerifyOtpRequest
import com.appmb.sdk.mbauth.data.dto.request.toMap
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.data.dto.response.RequestOtpData
import com.appmb.sdk.mbauth.data.dto.response.ResetPasswordDataModel
import com.appmb.sdk.mbauth.data.dto.response.VerifyOtpData
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.event.AnalyticsProperties
import com.appmb.sdk.mbauth.event.LoginAnalytics
import com.appmb.sdk.mbauth.event.SignupAnalytics
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.domain.game.MbGameRepository
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.ext.mapDataNull
import com.appmb.sdk.mbcore.network.json
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import com.appmb.sdk.mbcore.utils.sha256
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject


class MbAuthRepositoryImpl(
  val mbAuthDataSource: MbAuthDataSource,
  val gameRepository: MbGameRepository,
  val mbCommonDataSource: MbCommonDataSource,
  val mbSdkConfig: MbSdkConfig,
  private val mixpanel: AnalyticsProvider
) : MbAuthRepository {

  private suspend fun getDeviceAndGameIdEnsuringGameInfo(): Pair<String, String> = coroutineScope {
    val deviceIdDeferred = async { mbCommonDataSource.getDeviceId() }
    
    // Priority: 
    // 1. MbSdkConfig (manual/client set)
    // 2. DataStore (cached from previous remote fetch)
    val localGameId = mbSdkConfig.getGameId()?.takeIf { it.isNotBlank() && it != "0" }
    val cachedGameId = mbCommonDataSource.getGameId()
    
    val gameIdToUse = localGameId ?: cachedGameId.takeIf { it.isNotBlank() && it != "0" } ?: ""

    if (gameIdToUse.isBlank()) {
      gameRepository.fetchGameInfo()
    }

    val finalGameId = if (gameIdToUse.isBlank()) {
        mbCommonDataSource.getGameId().takeIf { it.isNotBlank() && it != "0" } ?: "1"
    } else {
        gameIdToUse
    }
    
    val deviceId = deviceIdDeferred.await()
    deviceId to finalGameId
  }

  override suspend fun loginByPhone(
    authParams: MbAuthParams,
  ): Either<NetworkError, LoginDataModel?> = coroutineScope {
    val (deviceId, gameId) = getDeviceAndGameIdEnsuringGameInfo()

    val signDeferred = async {
      val signString =
        "$deviceId|$gameId|${MbLoginByPhoneRequest.TYPE_PHONE}|${authParams.phone}|" +
            "${mbCommonDataSource.getPlatform()}|${mbSdkConfig.getAuthSdkVersion()}|" +
            "${mbSdkConfig.getAppVersionName()}|${mbCommonDataSource.getDeviceSecretId()}"
      Log.d("PHONE-Sign", "DeviceId|GameId|Type|Phone|Platform|SdkVersion|AppVersion|SecretId")
      Log.d("PHONE-Sign", "$signString")
      signString.sha256()
    }

    val serverId : Int? = if (!mbCommonDataSource.getServerId().isNullOrEmpty()) mbCommonDataSource.getServerId()?.toInt() else null

    val request = MbLoginByPhoneRequest(
      phone = authParams.phone,
      password = authParams.password,
      appVersion = mbSdkConfig.getAppVersionName(),
      platform = mbCommonDataSource.getPlatform(),
      gameId = gameId,
      serverId = serverId,
      sign = signDeferred.await(),
      deviceId = deviceId,
      sdkVersion = mbCommonDataSource.getSdkVersion(),
      adid = mbCommonDataSource.getAdid()
    )
    mixpanel.trackMap(
      eventName = LoginAnalytics.phoneLogin,
      properties = request.toMap()
    )
    val response = mbAuthDataSource.loginByPhone(request)
    when (response) {
      is Either.Right -> {
        response.value.data?.let {
          mbCommonDataSource.saveUserBlocked(it.userBlocked == true)
        }
        response.value.data?.let {
          mbCommonDataSource.saveServerId(it.serverId)
        }
        response.mapDataNull()
      }

      is Either.Left -> {
        response.value.let {
          if (it is NetworkError.ApiError) {
            mixpanel.trackMap(
              eventName = LoginAnalytics.phoneLogin,
              properties = mapOf(
                "error" to it.errorBody.message,
                "code" to it.errorBody.status
              )
            )
          }
          it.left()
        }
      }
    }
  }

  override suspend fun loginByGoogle(
    socialToken: String,
  ): Either<NetworkError, LoginDataModel?> = coroutineScope {
    val (deviceId, gameId) = getDeviceAndGameIdEnsuringGameInfo()

    val signDeferred = async {
      val signString =
        "$deviceId|${MbLoginSocialRequest.TYPE_GOOGLE}|$socialToken|" +
            "${mbCommonDataSource.getPlatform()}|${mbSdkConfig.getAuthSdkVersion()}|" +
            "${mbSdkConfig.getAppVersionName()}|$gameId|${mbCommonDataSource.getDeviceSecretId()}"
      Log.d("GOOGLE-Sign", "DeviceId|Type|Token|Platform|SdkVersion|AppVersion|GameId|SecretId")
      Log.d("GOOGLE-Sign", "$signString")
      signString.sha256()
    }

    val serverId : Int? = if (!mbCommonDataSource.getServerId().isNullOrEmpty()) mbCommonDataSource.getServerId()?.toInt() else null

    val request = MbLoginSocialRequest(
      token = socialToken,
      sdkVersion = mbCommonDataSource.getSdkVersion(),
      deviceId = deviceId,
      gameId = gameId,
      serverId = serverId,
      type = MbLoginSocialRequest.TYPE_GOOGLE,
      platform = mbCommonDataSource.getPlatform(),
      appVersion = mbSdkConfig.getAppVersionName(),
      sign = signDeferred.await(),
      adid = mbCommonDataSource.getAdid()
    )
    mixpanel.trackMap(
      eventName = LoginAnalytics.googleLogin,
      properties = json.encodeToJsonElement(request).jsonObject.mapValues { it.value.toString() }
    )
    val response = mbAuthDataSource.loginBySocial(request)
    when (response) {
      is Either.Right -> {
        response.value.data?.let {
          mbCommonDataSource.saveUserBlocked(it.userBlocked == true)
        }
        response.value.data?.let {
          mbCommonDataSource.saveServerId(it.serverId)
        }
        response.mapDataNull()
      }

      is Either.Left -> {
        response.value.let {
          if (it is NetworkError.ApiError) {
            mixpanel.trackMap(
              eventName = LoginAnalytics.googleLogin,
              properties = mapOf(
                "error" to it.errorBody.message,
                "code" to it.errorBody.status
              )
            )
          }
          it.left()
        }
      }
    }
  }

  override suspend fun loginByFacebook(
    socialToken: String,
  ): Either<NetworkError, LoginDataModel?> = coroutineScope {
    val (deviceId, gameId) = getDeviceAndGameIdEnsuringGameInfo()

    val signDeferred = async {
      val sign =
        "$deviceId|${MbLoginSocialRequest.TYPE_FACEBOOK}|$socialToken|" +
            "${mbCommonDataSource.getPlatform()}|${mbSdkConfig.getAuthSdkVersion()}|" +
            "${mbSdkConfig.getAppVersionName()}|$gameId|${mbCommonDataSource.getDeviceSecretId()}"

      sign.sha256()
    }

    val serverId : Int? = if (!mbCommonDataSource.getServerId().isNullOrEmpty()) mbCommonDataSource.getServerId()?.toInt() else null

    val request = MbLoginSocialRequest(
      token = socialToken,
      sdkVersion = mbCommonDataSource.getSdkVersion(),
      deviceId = deviceId,
      gameId = gameId,
      serverId = serverId,
      type = MbLoginSocialRequest.TYPE_FACEBOOK,
      platform = mbCommonDataSource.getPlatform(),
      appVersion = mbSdkConfig.getAppVersionName(),
      sign = signDeferred.await(),
      adid = mbCommonDataSource.getAdid()
    )
    mixpanel.trackMap(
      eventName = LoginAnalytics.facebookLogin,
      properties = json.encodeToJsonElement(request).jsonObject.mapValues { it.value.toString() }
    )
//    mbAuthDataSource.loginBySocial(request).mapDataNull()
    val response = mbAuthDataSource.loginBySocial(request)
    when (response) {
      is Either.Right -> {
        response.value.data?.let {
          mbCommonDataSource.saveUserBlocked(it.userBlocked == true)
        }
        response.mapDataNull()
      }

      is Either.Left -> {
        response.value.let {
          if (it is NetworkError.ApiError) {
            mixpanel.trackMap(
              eventName = LoginAnalytics.googleLogin,
              properties = mapOf(
                "error" to it.errorBody.message,
                "code" to it.errorBody.status
              )
            )
          }
          it.left()
        }
      }
    }
  }

  override suspend fun loginGuest(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> =
    coroutineScope {
      val (deviceId, gameId) = getDeviceAndGameIdEnsuringGameInfo()

      val signDeferred = async {
        val sign =
          "$deviceId|${MbLoginGuestRequest.TYPE_GUEST}|${mbCommonDataSource.getPlatform()}|" +
              "${mbSdkConfig.getAuthSdkVersion()}|" + "${mbSdkConfig.getAppVersionName()}|" +
              "$gameId|${mbCommonDataSource.getDeviceSecretId()}"
        Log.d("GUEST-Sign", "DeviceId|Type|Platform|SdkVersion|AppVersion|GameId|SecretId")
        Log.d("GUEST-Sign", "Sign: $sign")
        sign.sha256()
      }
      val serverId : Int? = if (!mbCommonDataSource.getServerId().isNullOrEmpty()) mbCommonDataSource.getServerId()?.toInt() else null
      val _sign = signDeferred.await()
      Log.d("GUEST-Sign", "Sign-SHA256: $_sign")
      val request = MbLoginGuestRequest(
        deviceId = deviceId,
        platform = mbCommonDataSource.getPlatform(),
        sdkVersion = mbCommonDataSource.getSdkVersion(),
        gameId = gameId,
        serverId = serverId,
        appVersion = mbSdkConfig.getAppVersionName(),
        sign = _sign,
        adid = mbCommonDataSource.getAdid()
      )
      mixpanel.trackMap(
        eventName = LoginAnalytics.guestLogin,
        properties = json.encodeToJsonElement(request).jsonObject.mapValues { it.value.toString() }
      )
      mbAuthDataSource.loginGuest(request).mapDataNull().flatMap {
        it?.toEntity()?.let { mbAuthData -> getSession().save(mbAuthData) }
        mbCommonDataSource.saveIsGuestUser(true)
        it?.serverId?.let { serverId ->
          mbCommonDataSource.saveServerId(serverId)
        }
        it?.copy(isGuest = true).right()
      }
    }

  override suspend fun register(
    authParams: MbAuthParams,
  ): Either<NetworkError, RegisterDataModel?> = coroutineScope {
    val (deviceId, gameId) = getDeviceAndGameIdEnsuringGameInfo()
    val registrationProfile = authParams.registrationProfile
    val otpVerifiedToken = registrationProfile?.playerOtpVerifiedToken
      ?: mbCommonDataSource.getOtpVerifiedToken()
    val signDeferred = async {
      val sign =
        "$deviceId|$gameId|${authParams.phone}|${authParams.password}|$otpVerifiedToken|${mbCommonDataSource.getPlatform()}|" +
            "${mbSdkConfig.getAuthSdkVersion()}|" + "${mbSdkConfig.getAppVersionName()}|" +
            mbCommonDataSource.getDeviceSecretId()
      sign.sha256()
    }

    val serverId : Int? = if (!mbCommonDataSource.getServerId().isNullOrEmpty()) mbCommonDataSource.getServerId()?.toInt() else null

    val request = MbRegisterRequest(
      deviceId = deviceId,
      device = Build.MODEL,
      platform = mbCommonDataSource.getPlatform(),
      sdkVersion = mbCommonDataSource.getSdkVersion(),
      appVersion = mbSdkConfig.getAppVersionName(),
      gameId = gameId.toIntOrNull(),
      serverId = serverId,
      otpVerifiedToken = otpVerifiedToken,
      type = "phone",
      phone = authParams.phone,
      password = authParams.password,
      fullName = registrationProfile?.personalInfo?.fullName,
      dateOfBirth = registrationProfile?.personalInfo?.dateOfBirth,
      gender = registrationProfile?.personalInfo?.gender,
      address = registrationProfile?.personalInfo?.address,
      consent = registrationProfile?.consent?.let { consent ->
        MbRegisterConsentRequest(
          legalAccepted = consent.legalAccepted,
          selfRegistrationAgeConfirmed = consent.selfRegistrationAgeConfirmed,
        )
      },
      guardian = registrationProfile?.guardian?.let { guardian ->
        MbRegisterGuardianRequest(
          fullName = guardian.fullName,
          dateOfBirth = guardian.dateOfBirth,
          phone = guardian.phone,
          address = guardian.address,
          otpVerifiedToken = guardian.otpVerifiedToken,
        )
      },
      sign = signDeferred.await(),
      adid = mbCommonDataSource.getAdid()
    )
    mbAuthDataSource.register(request).mapDataNull()
  }

  override suspend fun linkSocialAccount(
    mbAuthParams: MbAuthParams,
    socialToken: String,
  ): Either<NetworkError, LoginDataModel?> {
    if (mbAuthParams.linkAccountType == null) {
      mixpanel.trackMap(
        eventName = SignupAnalytics.linkToSocialAccount,
        properties = mapOf(
          AnalyticsProperties.failure to "Link Account is null"
        )
      )
      return NetworkError.DataNullError.left()
    }
    val linkAccountType: String = mbAuthParams.linkAccountType!!.name.lowercase()
    val deviceId = mbCommonDataSource.getDeviceId()
    val sign =
      "$deviceId|$socialToken|${linkAccountType}|${mbCommonDataSource.getDeviceSecretId()}"

    val serverId : Int? = if (!mbCommonDataSource.getServerId().isNullOrEmpty()) mbCommonDataSource.getServerId()?.toInt() else null

    val request = MbLinkSocialAccountRequest(
      token = socialToken,
      type = linkAccountType,
      deviceId = deviceId,
      serverId = serverId,
      gameId = mbCommonDataSource.getGameId().toInt(),
      appVersion = mbSdkConfig.getAppVersionName(),
      sdkVersion = mbSdkConfig.getAuthSdkVersion(),
      platform = "android",
      sign = sign.sha256()
    )
    return mbAuthDataSource.linkSocialAccount(request).mapDataNull()
  }

  override suspend fun linkPhoneAccount(mbAuthParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    val deviceId = mbCommonDataSource.getDeviceId()
    val otpVerifiedToken = mbCommonDataSource.getOtpVerifiedToken()
    val sign =
      "$deviceId|phone|${mbAuthParams.phone}|${mbAuthParams.password}|$otpVerifiedToken|${mbCommonDataSource.getDeviceSecretId()}"
    val request = MbRegisterRequest(
      deviceId = deviceId,
      platform = mbCommonDataSource.getPlatform(),
      sdkVersion = mbCommonDataSource.getSdkVersion(),
      appVersion = mbSdkConfig.getAppVersionName(),
      gameId = mbCommonDataSource.getGameId().toIntOrNull(),
      otpVerifiedToken = otpVerifiedToken,
      type = "phone",
      phone = mbAuthParams.phone,
      password = mbAuthParams.password,
      // Sign format - deviceId|type|phone|password|otpVerifiedToken|secretKey
      sign = sign.sha256()
    )
    return mbAuthDataSource.linkPhoneAccount(request).mapDataNull()
  }

  override suspend fun requestOtp(mbAuthParams: MbAuthParams): Either<NetworkError, RequestOtpData?> {
    val timeStamp = System.currentTimeMillis()
    val deviceId = mbCommonDataSource.getDeviceId()
    val deviceSecretKey = mbCommonDataSource.getDeviceSecretId()
    //sha256Base64Safe(deviceId|phone|type|timestamp|device-secret)
    val sign =
      "$deviceId|${mbAuthParams.phone}|${mbAuthParams.otpType}|$timeStamp|$deviceSecretKey"
    val request = MbRequestOtpRequest(
      deviceId = deviceId,
      phone = mbAuthParams.phone.orEmpty(),
      type = mbAuthParams.otpType.orEmpty(),
      timestamp = timeStamp,
      sign = sign.sha256(),
    )
    return mbAuthDataSource.requestOtp(request).mapDataNull()
  }

  override suspend fun verifyOtp(mbAuthParams: MbAuthParams): Either<NetworkError, VerifyOtpData?> {
    val timeStamp = System.currentTimeMillis()
    val deviceId = mbCommonDataSource.getDeviceId()
    val deviceSecretKey = mbCommonDataSource.getDeviceSecretId()
    //sha256Base64Safe(deviceId|phone|type|timestamp|device-secret)
    val sign =
      "$deviceId|${mbAuthParams.phone}|${mbAuthParams.otpType}|$timeStamp|$deviceSecretKey"
    val request = MbVerifyOtpRequest(
      deviceId = deviceId,
      phone = mbAuthParams.phone.orEmpty(),
      type = mbAuthParams.otpType.orEmpty(),
      timestamp = timeStamp,
      otp = mbAuthParams.otp.orEmpty(),
      sign = sign.sha256(),
    )
    return mbAuthDataSource.verifyOtp(request).mapDataNull()
  }

  override suspend fun resetPassword(mbAuthParams: MbAuthParams): Either<NetworkError, ResetPasswordDataModel?> {
    val request = MbResetPasswordRequest(
      deviceId = mbCommonDataSource.getDeviceId(),
      platform = mbCommonDataSource.getPlatform(),
      sdkVersion = mbCommonDataSource.getSdkVersion(),
      appVersion = mbSdkConfig.getAppVersionName(),
      otpVerifiedToken = mbCommonDataSource.getOtpVerifiedToken(),
      type = "phone",
      phone = mbAuthParams.phone,
      password = mbAuthParams.password,
    )
    return mbAuthDataSource.resetPassword(request).mapDataNull()
  }

  override suspend fun deactivateAccount(): Either<NetworkError, Unit> =
    mbAuthDataSource.deactivateAccount().fold(
      ifLeft = {
        it.left()
      },
      ifRight = {
        Unit.right()
      }
    )

  override suspend fun logout(): Either<NetworkError, Unit> {
    return mbAuthDataSource.logout().fold(
      ifLeft = {
        it.left()
      },
      ifRight = {
        Unit.right()
      }
    )
  }
}
