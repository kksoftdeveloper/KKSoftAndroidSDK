package com.appmb.sdk.mbcore.config

class MbSdkConfig {
  private var appId: String? = null
  private var gameId: String? = null
  private var serverClientId: String? = null
  private var appVersionName: String? = null
  private var baseUrl: String? = null
  private var authSdkVersion: String? = null
  private var paymentSdkVersion: String? = null
  private var trackingConfig: TrackingConfig? = null

  // Auth specific configs
  private var googleClientId: String? = null
  private var facebookAppId: String? = null
  private var facebookClientToken: String? = null

  fun isValid(): Boolean {
    return appId.isNullOrEmpty().not()
  }

  fun getAppId(): String? {
    return appId
  }

  fun getGameId(): String? {
    return gameId.normalizedGameIdOrDefault()
  }

  fun getServerClientId(): String? {
    return this@MbSdkConfig.serverClientId
  }

  fun getAppVersionName(): String? {
    return appVersionName
  }

  fun getBaseUrl(): String? {
    return baseUrl
  }

  fun getAuthSdkVersion(): String? {
    return authSdkVersion
  }

  fun setAuthSdkVersion(authSdkVersion: String) {
    this.authSdkVersion = authSdkVersion
  }

  fun setServerClientId(serverId: String) {
    this.serverClientId = serverId
  }

  fun getPaymentSdkVersion(): String? {
    return paymentSdkVersion
  }

  fun setPaymentSdkVersion(paymentSdkVersion: String) {
    this.paymentSdkVersion = paymentSdkVersion
  }

  fun getTrackingConfig(): TrackingConfig? {
    return trackingConfig
  }

  fun getGoogleClientId(): String? = googleClientId
  fun getFacebookAppId(): String? = facebookAppId
  fun getFacebookClientToken(): String? = facebookClientToken

  class Builder(
    internal var appId: String? = null,
    internal var gameId: String? = null,
    internal var appVersionName: String? = null,
    internal var baseUrl: String? = null,
    internal var authSdkVersion: String? = null,
    internal var paymentSdkVersion: String? = null,
    internal var serverClientId: String? = null,
    internal var trackingConfig: TrackingConfig? = null,
    internal var googleClientId: String? = null,
    internal var facebookAppId: String? = null,
    internal var facebookClientToken: String? = null
  ) {

    private constructor(builder: Builder) : this(
      appId = builder.appId,
      gameId = builder.gameId,
      appVersionName = builder.appVersionName,
      baseUrl = builder.baseUrl,
      authSdkVersion = builder.authSdkVersion,
      paymentSdkVersion = builder.paymentSdkVersion,
      serverClientId = builder.serverClientId,
      trackingConfig = builder.trackingConfig,
      googleClientId = builder.googleClientId,
      facebookAppId = builder.facebookAppId,
      facebookClientToken = builder.facebookClientToken
    )

    fun setAppId(appId: String) = apply {
      this.appId = appId
    }

    fun setGameId(gameId: String?) = apply {
      this.gameId = gameId.normalizedGameIdOrDefault()
    }

    fun setAppVersionName(appVersionName: String) = apply {
      this.appVersionName = appVersionName
    }

    fun setBaseUrl(baseUrl: String?) = apply {
      this.baseUrl = baseUrl
    }

    fun setAuthSdkVersion(authSdkVersion: String) = apply {
      this.authSdkVersion = authSdkVersion
    }

    fun setServerClientId(serverClientId: String?) = apply {
      this.serverClientId = serverClientId
    }

    fun setTrackingConfig(trackingConfig: TrackingConfig?) = apply {
      this.trackingConfig = trackingConfig
    }

    fun setGoogleClientId(clientId: String?) = apply {
      this.googleClientId = clientId
    }

    fun setFacebookAppId(appId: String?) = apply {
      this.facebookAppId = appId
    }

    fun setFacebookClientToken(clientToken: String?) = apply {
      this.facebookClientToken = clientToken
    }

    fun build() = MbSdkConfig().apply {
      this.appId = this@Builder.appId
      this.gameId = this@Builder.gameId.normalizedGameIdOrDefault()
      this.appVersionName = this@Builder.appVersionName
      this.baseUrl = this@Builder.baseUrl
      this.authSdkVersion = this@Builder.authSdkVersion
      this.paymentSdkVersion = this@Builder.paymentSdkVersion
      this.serverClientId = this@Builder.serverClientId
      this.trackingConfig = this@Builder.trackingConfig
      this.googleClientId = this@Builder.googleClientId
      this.facebookAppId = this@Builder.facebookAppId
      this.facebookClientToken = this@Builder.facebookClientToken
    }
  }
}

private fun String?.normalizedGameIdOrDefault(): String {
  return this?.trim()?.toIntOrNull()?.takeIf { it >= 1 }?.toString() ?: "1"
}
