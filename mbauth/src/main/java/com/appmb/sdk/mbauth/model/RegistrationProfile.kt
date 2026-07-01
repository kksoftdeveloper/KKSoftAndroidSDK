package com.appmb.sdk.mbauth.model

data class RegistrationProfile(
  val playerOtpVerifiedToken: String?,
  val personalInfo: RegistrationInfo,
  val consent: RegistrationConsent,
  val guardian: GuardianRegistrationInfo? = null,
)

data class RegistrationInfo(
  val fullName: String,
  val dateOfBirth: String,
  val gender: String?,
  val address: String?,
)

data class RegistrationConsent(
  val legalAccepted: Boolean,
  val selfRegistrationAgeConfirmed: Boolean,
)

data class GuardianRegistrationInfo(
  val fullName: String,
  val dateOfBirth: String,
  val phone: String,
  val address: String?,
  val otpVerifiedToken: String? = null,
)
