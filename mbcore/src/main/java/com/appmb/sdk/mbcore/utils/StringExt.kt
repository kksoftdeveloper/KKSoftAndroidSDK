package com.appmb.sdk.mbcore.utils

import java.security.MessageDigest
import java.util.Base64


fun String.Companion.empty() = ""

fun String.sha256(): String {
  val digest = MessageDigest.getInstance("SHA-256")
  val hashBytes = digest.digest(this.toByteArray(Charsets.UTF_8))
  return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes)
}

fun String.toBoolean(): Boolean {
  return this.lowercase() == "true"
}
