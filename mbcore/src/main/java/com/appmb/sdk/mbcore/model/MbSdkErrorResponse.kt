package com.appmb.sdk.mbcore.model

import android.annotation.SuppressLint
import android.content.Context
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.DeactivatedOrNotFound
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.AccountNotExist
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.AppNotConfigured
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.AppNotConfiguredFacebook
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.AppNotConfiguredGameServer
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.AppNotConfiguredGoogle
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.AppNotFound
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.DuplicatedPhoneError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.FacebookAuthenticateError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.FacebookUnknownError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.GoogleAuthenticateError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.GooglePlayServiceUnavailable
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.GoogleUnknownError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.InvalidPhoneError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.MatchError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.NewPasswordRepeated
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.NotFound
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.OTPError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.OTPExpired
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.OTPInvalid
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.OTPRequestManyTime
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.PasswordValidationError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.PhoneOrPasswordInvalid
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.SDKNotInitialized
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.SDKSignatureError
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.SocialAccountLinked
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.SocialUserCancels
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.Success
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.TokenExpired
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.Unauthorized
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse.UnknownSupported
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MbSdkErrorResponse(
  val status: Int?,
  val code: Int?,
  val message: String
)

enum class AuthErrorCodeResponse(val code: Int) {
  Success(1),
  MatchError(0),
  OTPError(-1),
  OTPInvalid(-10),
  OTPExpired(-81),
  OTPRequestManyTime(-80),
  NewPasswordRepeated(-60),
  PhoneOrPasswordInvalid(-300),
  PasswordValidationError(-110),
  DuplicatedPhoneError(-2),
  InvalidPhoneError(-3),
  SocialAccountLinked(-20),
  AccountNotExist(-30),
  TokenExpired(-200),
  Unauthorized(-401),
  NotFound(-400),
  UnknownSupported(-405),
  DeactivatedOrNotFound(-404),
  AppNotFound(-1001),
  AppNotConfigured(-1002),
  AppNotConfiguredGame(-1003),
  AppNotConfiguredGameServer(-1004),
  AppNotConfiguredFacebook(-1005),
  AppNotConfiguredGoogle(-1006),
  FacebookUnknownError(-2001),
  FacebookAuthenticateError(-2002),
  GoogleUnknownError(-3001),
  GoogleAuthenticateError(-3002),
  SocialUserCancels(-4001),
  SDKNotInitialized(-5001),
  SDKSignatureError(-5002),
  GooglePlayServiceUnavailable(-6000),
  UnknownError(-500);

  val description: String
    get() = when (this) {
      Success -> "Success"
      MatchError -> "Some error from given parameters"
      PasswordValidationError -> "Passwords must match and meet criteria."
      OTPError -> "Failed to verify OTP"
      OTPExpired -> "OTP is expired"
      OTPRequestManyTime -> "Too many OTP requests. Please try again in 60 seconds"
      DuplicatedPhoneError -> "Phone number has been existed"
      InvalidPhoneError -> "Phone number has not signed up or OTP has not verified successful"
      AccountNotExist -> "Account is not registered"
      DeactivatedOrNotFound -> "Account is deleted"
      OTPInvalid -> "OTP is invalid"
      PhoneOrPasswordInvalid -> "Invalid phone or password."
      TokenExpired -> "Token has expired"
      NewPasswordRepeated -> "New password cannot be the same as the old password."
      Unauthorized -> "Unauthenticated"
      UnknownSupported -> "Unsupported the request"
      UnknownError -> "Something has gone badly"
      AppNotFound -> "App game has not registered"
      AppNotConfigured -> "App has not completed configuration"
      AppNotConfiguredGame -> "App has not configured game"
      AppNotConfiguredGameServer -> "App has not configured game servers"
      AppNotConfiguredFacebook -> "App has not completed configuration for Facebook"
      AppNotConfiguredGoogle -> "App has not completed configuration for Google"
      SocialUserCancels -> "User has cancelled"
      FacebookUnknownError -> "Facebook Unknown Error"
      FacebookAuthenticateError -> "Facebook Authenticate Error"
      GoogleUnknownError -> "Google Unknown Error"
      GoogleAuthenticateError -> "Google Authenticate Error"
      SDKNotInitialized -> "SDK has not initialized"
      SDKSignatureError -> "SDK has signed incorrect"
      SocialAccountLinked -> "Account is already linked to another one."
      GooglePlayServiceUnavailable -> "Google play service is unavailable."
      NotFound -> "Account is deleted or not found."
    }
}

fun String.localization(context: Context): String {
  return when (this) {
    Success.description -> context.getString(com.appmb.sdk.mbcore.R.string.success)
    MatchError.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_match_error)
    PasswordValidationError.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_password_validation_error)
    OTPError.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_error)
    OTPExpired.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_expired)
    OTPRequestManyTime.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_request_many_time)
    DuplicatedPhoneError.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_duplicated_phone_error)
    InvalidPhoneError.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_invalid_phone_error)
    AccountNotExist.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_account_not_exist)
    DeactivatedOrNotFound.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_account_deactivated)
    OTPInvalid.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_invalid)
    PhoneOrPasswordInvalid.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_phone_pass_invalid)
    TokenExpired.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_token_expired)
    NewPasswordRepeated.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_new_password_repeated)
    Unauthorized.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unauthorized)
    UnknownSupported.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_supported)
    AuthErrorCodeResponse.UnknownError.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_error)
    AppNotFound.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_found)
    AppNotConfigured.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured)
    AppNotConfiguredGameServer.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_game_server)
    AppNotConfiguredFacebook.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_facebook)
    AppNotConfiguredGoogle.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_google)
    SocialUserCancels.description -> context.getString(com.appmb.sdk.mbcore.R.string.auth_social_user_cancel)
    FacebookUnknownError.description -> context.getString(com.appmb.sdk.mbcore.R.string.FacebookUnknownError)
    FacebookAuthenticateError.description -> context.getString(com.appmb.sdk.mbcore.R.string.FacebookAuthenticateError)
    GoogleUnknownError.description -> context.getString(com.appmb.sdk.mbcore.R.string.GoogleUnknownError)
    GoogleAuthenticateError.description -> context.getString(com.appmb.sdk.mbcore.R.string.GoogleAuthenticateError)
    SDKNotInitialized.description -> context.getString(com.appmb.sdk.mbcore.R.string.SDKNotInitialized)
    SDKSignatureError.description -> context.getString(com.appmb.sdk.mbcore.R.string.SDKSignatureError)
    SocialAccountLinked.description -> context.getString(com.appmb.sdk.mbcore.R.string.SocialAccountLinked)
    GooglePlayServiceUnavailable.description -> context.getString(com.appmb.sdk.mbcore.R.string.google_play_service_unavailable)
    NotFound.description -> context.getString(com.appmb.sdk.mbcore.R.string.NotFound)
    else -> {
      context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_error)
    }
  }
}
fun Int.localization(context: Context): String {
  return when (this) {
    Success.code -> context.getString(com.appmb.sdk.mbcore.R.string.success)
    MatchError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_match_error)
    PasswordValidationError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_password_validation_error)
    OTPError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_error)
    OTPExpired.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_expired)
    OTPRequestManyTime.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_request_many_time)
    DuplicatedPhoneError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_duplicated_phone_error)
    InvalidPhoneError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_invalid_phone_error)
    AccountNotExist.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_account_not_exist)
    DeactivatedOrNotFound.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_account_deactivated)
    OTPInvalid.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_invalid)
    PhoneOrPasswordInvalid.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_phone_pass_invalid)
    TokenExpired.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_token_expired)
    NewPasswordRepeated.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_new_password_repeated)
    Unauthorized.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unauthorized)
    UnknownSupported.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_supported)
    AuthErrorCodeResponse.UnknownError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_error)
    AppNotFound.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_found)
    AppNotConfigured.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured)
    AppNotConfiguredGameServer.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_game_server)
    AppNotConfiguredFacebook.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_facebook)
    AppNotConfiguredGoogle.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_google)
    SocialUserCancels.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_social_user_cancel)
    FacebookUnknownError.code -> context.getString(com.appmb.sdk.mbcore.R.string.FacebookUnknownError)
    FacebookAuthenticateError.code -> context.getString(com.appmb.sdk.mbcore.R.string.FacebookAuthenticateError)
    GoogleUnknownError.code -> context.getString(com.appmb.sdk.mbcore.R.string.GoogleUnknownError)
    GoogleAuthenticateError.code -> context.getString(com.appmb.sdk.mbcore.R.string.GoogleAuthenticateError)
    SDKNotInitialized.code -> context.getString(com.appmb.sdk.mbcore.R.string.SDKNotInitialized)
    SDKSignatureError.code -> context.getString(com.appmb.sdk.mbcore.R.string.SDKSignatureError)
    SocialAccountLinked.code -> context.getString(com.appmb.sdk.mbcore.R.string.SocialAccountLinked)
    GooglePlayServiceUnavailable.code -> context.getString(com.appmb.sdk.mbcore.R.string.google_play_service_unavailable)
    NotFound.code -> context.getString(com.appmb.sdk.mbcore.R.string.NotFound)
    else -> {
      context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_error)
    }
  }
}

fun MbSdkErrorResponse.localization(context: Context): MbSdkErrorResponse {
  val localizedDescription = when (this.status) {
    AuthErrorCodeResponse.Success.code -> context.getString(com.appmb.sdk.mbcore.R.string.success)
    AuthErrorCodeResponse.MatchError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_match_error)
    AuthErrorCodeResponse.PasswordValidationError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_password_validation_error)
    AuthErrorCodeResponse.OTPError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_error)
    AuthErrorCodeResponse.OTPExpired.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_expired)
    AuthErrorCodeResponse.OTPRequestManyTime.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_request_many_time)
    AuthErrorCodeResponse.DuplicatedPhoneError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_duplicated_phone_error)
    AuthErrorCodeResponse.InvalidPhoneError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_invalid_phone_error)
    AuthErrorCodeResponse.AccountNotExist.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_account_not_exist)
    AuthErrorCodeResponse.DeactivatedOrNotFound.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_account_deactivated)
    AuthErrorCodeResponse.OTPInvalid.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_otp_invalid)
    AuthErrorCodeResponse.PhoneOrPasswordInvalid.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_phone_pass_invalid)
    AuthErrorCodeResponse.TokenExpired.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_token_expired)
    AuthErrorCodeResponse.NewPasswordRepeated.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_new_password_repeated)
    AuthErrorCodeResponse.Unauthorized.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unauthorized)
    AuthErrorCodeResponse.UnknownSupported.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_supported)
    AuthErrorCodeResponse.UnknownError.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_error)
    AuthErrorCodeResponse.AppNotFound.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_found)
    AuthErrorCodeResponse.AppNotConfigured.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured)
    AuthErrorCodeResponse.AppNotConfiguredGame.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_game)
    AuthErrorCodeResponse.AppNotConfiguredGameServer.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_game_server)
    AuthErrorCodeResponse.AppNotConfiguredFacebook.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_facebook)
    AuthErrorCodeResponse.AppNotConfiguredGoogle.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_app_not_configured_google)
    AuthErrorCodeResponse.SocialUserCancels.code -> context.getString(com.appmb.sdk.mbcore.R.string.auth_social_user_cancel)
    AuthErrorCodeResponse.FacebookUnknownError.code -> context.getString(com.appmb.sdk.mbcore.R.string.FacebookUnknownError)
    AuthErrorCodeResponse.FacebookAuthenticateError.code -> context.getString(com.appmb.sdk.mbcore.R.string.FacebookAuthenticateError)
    AuthErrorCodeResponse.GoogleUnknownError.code -> context.getString(com.appmb.sdk.mbcore.R.string.GoogleUnknownError)
    AuthErrorCodeResponse.GoogleAuthenticateError.code -> context.getString(com.appmb.sdk.mbcore.R.string.GoogleAuthenticateError)
    AuthErrorCodeResponse.SDKNotInitialized.code -> context.getString(com.appmb.sdk.mbcore.R.string.SDKNotInitialized)
    AuthErrorCodeResponse.SDKSignatureError.code -> context.getString(com.appmb.sdk.mbcore.R.string.SDKSignatureError)
    AuthErrorCodeResponse.SocialAccountLinked.code -> context.getString(com.appmb.sdk.mbcore.R.string.SocialAccountLinked)
    AuthErrorCodeResponse.GooglePlayServiceUnavailable.code -> context.getString(com.appmb.sdk.mbcore.R.string.google_play_service_unavailable)
    AuthErrorCodeResponse.NotFound.code -> context.getString(com.appmb.sdk.mbcore.R.string.NotFound)
    else -> {
      context.getString(com.appmb.sdk.mbcore.R.string.auth_unknown_error)
    }
  }
  return MbSdkErrorResponse(
    status = this.status,
    code = this.code,
    message = localizedDescription
  )
}