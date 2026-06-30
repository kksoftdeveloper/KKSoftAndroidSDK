package com.appmb.sdk.mbauth.model

import android.content.Context
import com.appmb.sdk.mbauth.core.provider.LoginType
import com.appmb.sdk.mbauth.data.dto.request.MbVerifyOtpRequest
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Class representing the parameters required for authentication.
 */
class MbAuthParams {
  internal var context: Context? = null
  internal var type: LoginType? = null

  internal var serverId: String? = null
  internal var osVersion: String? = null
  internal var gameId: Int? = null

  // App info
  internal var appVersion: String? = null
  internal var appPackageName: String? = null

  // Email or phone
  internal var email: String? = null
  internal var phone: String? = null
  internal var password: String? = null

  // OTP Verify params
  internal var otp: String? = null
  internal var otpType: String? = null

  // Link account type
  internal var linkAccountType: LinkAccountType? = null

  // Google SignIn Account
  internal var googleAccount: GoogleSignInAccount? = null

  private constructor()

  /**
   * Builder class for constructing `MbAuthParams` instances.
   */
  class Builder(
    private var context: Context? = null,
    private var type: LoginType = LoginType.PHONE,
    private var email: String? = null,
    private var phone: String? = null,
    private var password: String? = null,
    // Common fields
    private var serverId: String? = null,
    private var osVersion: String? = null,
    private var gameId: Int? = null,
    // App info
    private var appVersion: String? = null,
    private var appPackageName: String? = null,
    // OTP
    private var otp: String? = null,
    private var otpType: String? = null,
    // Link account type
    private var linkAccountType: LinkAccountType? = null,
    // Google signIn account
    private var googleAccount: GoogleSignInAccount? = null,
  ) {

    private constructor(builder: Builder) : this(
      context = builder.context,
      type = builder.type,
      email = builder.email,
      phone = builder.phone,
      password = builder.password,
      serverId = builder.serverId,
      osVersion = builder.osVersion,
      gameId = builder.gameId,
      appVersion = builder.appVersion,
      appPackageName = builder.appPackageName,
      otp = builder.otp,
      otpType = builder.otpType,
      linkAccountType = builder.linkAccountType,
      googleAccount = builder.googleAccount
    )

    /**
     * Sets the context.
     * @param context The context.
     * @return The builder instance.
     */
    fun setContext(context: Context) = apply {
      this.context = context
    }

    /**
     * Sets the server ID.
     * @param serverId The server ID.
     * @return The builder instance.
     */
    fun setServerId(serverId: String) = apply {
      this.serverId = serverId
    }

    fun setGameId(gameId: Int) = apply {
      this.gameId = gameId
    }

    /**
     * Sets the OS version.
     * @param osVersion The OS version.
     * @return The builder instance.
     */
    fun setOsVersion(osVersion: String) = apply {
      this.osVersion = osVersion
    }

    /**
     * Sets the login type.
     * @param type The login type.
     * @return The builder instance.
     */
    fun setLoginType(type: LoginType) = apply {
      this.type = type
    }

    /**
     * Sets the email.
     * @param email The email.
     * @return The builder instance.
     */
    fun setEmail(email: String) = apply {
      this.email = email
    }

    /**
     * Sets the phone number.
     * @param phone The phone number.
     * @return The builder instance.
     */
    fun setPhone(phone: String) = apply {
      this.phone = phone
    }

    /**
     * Sets the password.
     * @param password The password.
     * @return The builder instance.
     */
    fun setPassword(password: String) = apply {
      this.password = password
    }

    /**
     * Sets the app version.
     * @param appVersion The app version.
     * @return The builder instance.
     */
    fun setAppVersion(appVersion: String) = apply {
      this.appVersion = appVersion
    }

    /**
     * Sets the app package name.
     * @param appPackageName The app package name.
     * @return The builder instance.
     */
    fun setAppPackageName(appPackageName: String) = apply {
      this.appPackageName = appPackageName
    }

    /**
     * Sets the OTP.
     * @param otp The OTP.
     * @return The builder instance.
     */
    fun setOtp(otp: String) = apply {
      this.otp = otp
    }

    /**
     * Sets the OTP type.
     * @param otpType The OTP type.
     * @return The builder instance.
     */
    fun setOtpType(otpType: String) = apply {
      this.otpType = otpType
    }

    /**
     * Sets the link account type
     * @param linkAccountType The link account type
     * @return The builder instance.
     */
    fun setLinkAccountType(linkAccountType: LinkAccountType) = apply {
      this.linkAccountType = linkAccountType
    }

    /**
     * Builds and returns an `MbAuthParams` instance.
     * @return The constructed `MbAuthParams` instance.
     */
    fun build() = MbAuthParams().apply {
      context = this@Builder.context
      type = this@Builder.type
      email = this@Builder.email
      phone = this@Builder.phone
      password = this@Builder.password
      serverId = this@Builder.serverId
      gameId = this@Builder.gameId
      osVersion = this@Builder.osVersion
      appPackageName = this@Builder.appPackageName
      appVersion = this@Builder.appVersion
      otp = this@Builder.otp
      otpType = this@Builder.otpType
      linkAccountType = this@Builder.linkAccountType
      googleAccount = this@Builder.googleAccount
    }
  }

  companion object {

    // OTP type
    const val OTP_TYPE_PARAM_REGISTRATION = MbVerifyOtpRequest.OTP_TYPE_REGISTRATION
    const val OTP_TYPE_PARAM_LINK_PHONE_ACCOUNT = "LINK_PHONE_ACCOUNT"
    const val OTP_TYPE_PARAM_FORGOT_PASSWORD = MbVerifyOtpRequest.OTP_TYPE_FORGOT_PASSWORD

    /**
     * Builds an `MbAuthParams` instance for phone login.
     *
     * @param phone The phone number.
     * @param password The password.
     * @param osVersion The OS version.
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildLoginByPhone(
      phone: String,
      password: String,
      osVersion: String,
      gameId: Int?,
      appVersion: String?,
      appPackageName: String?
    ) = Builder(
      type = LoginType.PHONE,
      phone = phone,
      password = password,
      osVersion = osVersion,
      gameId = gameId,
      appVersion = appVersion,
      appPackageName = appPackageName
    ).build()

    /**
     * Builds an `MbAuthParams` instance for Google login.
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildLoginByGoogle(
      context: Context?,
      osVersion: String,
      gameId: Int?,
      account: GoogleSignInAccount,
      appVersion: String?,
      appPackageName: String?
    ) =
      Builder(
        context = context,
        type = LoginType.GOOGLE,
        osVersion = osVersion,
        gameId = gameId,
        googleAccount = account,
        appVersion = appVersion,
        appPackageName = appPackageName
      ).build()

    /**
     * Builds an `MbAuthParams` instance for Facebook login.
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildLoginByFacebook(
      context: Context,
      osVersion: String,
      gameId: Int?,
      appVersion: String?,
      appPackageName: String?
    ) =
      Builder(
        context = context,
        osVersion = osVersion,
        gameId = gameId,
        type = LoginType.FACEBOOK,
        appVersion = appVersion,
        appPackageName = appPackageName
      ).build()

    /**
     * Builds an `MbAuthParams` instance for guest login
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildLoginByGuest(
      osVersion: String,
      gameId: Int?,
      appVersion: String?,
      appPackageName: String?
    ) = Builder(
      type = LoginType.GUEST,
      osVersion = osVersion,
      gameId = gameId,
      appVersion = appVersion,
      appPackageName = appPackageName
    ).build()

    /**
     * Builds an `MbAuthParams` instance for request OTP
     *
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildRequestOtp(
      phone: String,
      type: String,
    ) = Builder(
      phone = phone,
      otpType = when (type) {
        OTP_TYPE_PARAM_REGISTRATION,
        OTP_TYPE_PARAM_LINK_PHONE_ACCOUNT -> OTP_TYPE_PARAM_REGISTRATION
        else -> OTP_TYPE_PARAM_FORGOT_PASSWORD
      },
    ).build()

    /**
     * Builds an `MbAuthParams` instance for verify OTP
     *
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildVerifyOtp(
      phone: String,
      otpType: String,
      otp: String,
    ) = Builder(
      phone = phone,
      otpType = when (otpType) {
        OTP_TYPE_PARAM_REGISTRATION,
        OTP_TYPE_PARAM_LINK_PHONE_ACCOUNT -> OTP_TYPE_PARAM_REGISTRATION
        else -> OTP_TYPE_PARAM_FORGOT_PASSWORD
      },
      otp = otp,
    ).build()

    /**
     * Builds an `MbAuthParams` instance for Register
     */
    fun buildRegister(
      phone: String?,
      password: String
    ) = Builder(
      type = LoginType.PHONE,
      phone = phone,
      password = password,
    ).build()

    /**
     * Builds an `MbAuthParams` instance for link account social
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildLinkSocialAccount(
      context: Context,
      linkAccountType: LinkAccountType,
      googleAccount: GoogleSignInAccount?,
    ) = Builder(
      context = context,
      linkAccountType = linkAccountType,
      type = when (linkAccountType) {
        LinkAccountType.GOOGLE -> LoginType.GOOGLE
        LinkAccountType.FACEBOOK -> LoginType.FACEBOOK
        else -> LoginType.PHONE
      },
      googleAccount = googleAccount
    ).build()

    /**
     * Builds an `MbAuthParams` instance for link phone account
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildLinkPhoneAccount(
      phone: String,
      password: String,
      linkAccountType: LinkAccountType = LinkAccountType.PHONE
    ) = Builder(
      phone = phone,
      password = password,
      type = LoginType.PHONE,
      linkAccountType = linkAccountType
    ).build()

    /**
     * Builds an `MbAuthParams` instance for reset password
     * @return The constructed `MbAuthParams` instance.
     */
    fun buildResetPassword(
      phone: String,
      password: String,
    ) = Builder(
      phone = phone,
      password = password,
    ).build()
  }
}

enum class LinkAccountType {
  PHONE,
  GOOGLE,
  FACEBOOK
}

