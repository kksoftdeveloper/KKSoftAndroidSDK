package com.appmb.sdk.mbauth.core.provider

import com.appmb.sdk.mbauth.domain.MbAuthRepository

/**
 * Factory class for creating instances of `MbAuthProvider` based on the login type.
 */
internal class MbAuthProviderFactory(
  private val mbAuthRepository: MbAuthRepository,
) {

  /**
   * Returns an instance of `MbAuthProvider` based on the provided login type.
   *
   * @param type The type of login for which the provider is needed.
   * @return An instance of `MbAuthProvider` corresponding to the login type.
   */
  fun getProvider(type: LoginType): MbAuthProvider {
    return when (type) {
      LoginType.EMAIL -> LoginByEmailProvider()
      LoginType.PHONE -> AuthenticationByPhone(mbAuthRepository)
      LoginType.GOOGLE -> AuthenticationByGoogleProvider(mbAuthRepository)
      LoginType.FACEBOOK -> AuthenticationByFacebookProvider(mbAuthRepository)
      LoginType.GUEST -> LoginGuestProvider(mbAuthRepository)
    }
  }
}