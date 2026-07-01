package com.appmb.sdk.mbauth.ui.registration

enum class RegistrationStep {
  Phone,
  UserOtp,
  PersonalInfo,
  GuardianInfo,
  GuardianOtp,
  Password
}

enum class ProfileCompletionStep {
  Phone,
  UserOtp,
  PersonalInfo,
  GuardianInfo,
  GuardianOtp
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

  fun profileCompletionSteps(
    isUnder16: Boolean,
    requiresUserPhoneVerification: Boolean,
  ): List<ProfileCompletionStep> = buildList {
    if (requiresUserPhoneVerification) {
      add(ProfileCompletionStep.Phone)
      add(ProfileCompletionStep.UserOtp)
    }
    add(ProfileCompletionStep.PersonalInfo)
    if (isUnder16) {
      add(ProfileCompletionStep.GuardianInfo)
      add(ProfileCompletionStep.GuardianOtp)
    }
  }

  fun profileCompletionLabel(
    step: ProfileCompletionStep,
    isUnder16: Boolean,
    requiresUserPhoneVerification: Boolean,
  ): String {
    val steps = profileCompletionSteps(isUnder16, requiresUserPhoneVerification)
    val index = steps.indexOf(step).takeIf { it >= 0 } ?: 0
    return "B\u01b0\u1edbc ${index + 1}"
  }
}
