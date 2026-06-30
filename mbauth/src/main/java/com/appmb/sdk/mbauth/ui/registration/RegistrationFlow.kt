package com.appmb.sdk.mbauth.ui.registration

enum class RegistrationStep {
  Phone,
  UserOtp,
  PersonalInfo,
  GuardianInfo,
  GuardianOtp,
  Password
}

object RegistrationFlow {
  fun steps(isUnder16: Boolean): List<RegistrationStep> =
    if (isUnder16) {
      listOf(
        RegistrationStep.Phone,
        RegistrationStep.UserOtp,
        RegistrationStep.PersonalInfo,
        RegistrationStep.GuardianInfo,
        RegistrationStep.GuardianOtp,
        RegistrationStep.Password
      )
    } else {
      listOf(
        RegistrationStep.Phone,
        RegistrationStep.UserOtp,
        RegistrationStep.PersonalInfo,
        RegistrationStep.Password
      )
    }

  fun label(step: RegistrationStep, isUnder16: Boolean): String {
    val index = steps(isUnder16).indexOf(step).takeIf { it >= 0 } ?: 0
    return "B\u01b0\u1edbc ${index + 1}"
  }
}
