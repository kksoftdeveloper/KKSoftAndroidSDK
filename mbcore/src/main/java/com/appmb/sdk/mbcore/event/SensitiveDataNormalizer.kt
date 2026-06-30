package com.appmb.sdk.mbcore.event

import org.json.JSONObject
import java.net.URL

/**
 * Utility class to normalize sensitive data before sending to analytics providers.
 * 
 * This class filters out sensitive fields such as:
 * - Passwords
 * - Tokens (access tokens, refresh tokens, OTP tokens, social tokens)
 * - Authorization headers
 * - API keys
 * - Other sensitive credentials
 * - Base URLs from event names
 */
object SensitiveDataNormalizer {
  
  /**
   * Placeholder value used to replace sensitive data
   */
  private const val REDACTED_VALUE = "[REDACTED]"
  
  /**
   * Set of field names that should be normalized (case-insensitive)
   */
  private val sensitiveFieldNames = setOf(
    "password",
    "pwd",
    "pass",
    "token",
    "accesstoken",
    "access_token",
    "refreshtoken",
    "refresh_token",
    "otpverifiedtoken",
    "otp_verified_token",
    "otp",
    "socialtoken",
    "social_token",
    "apikey",
    "api_key",
    "apisecret",
    "api_secret",
    "secret",
    "secretkey",
    "secret_key",
    "authorization",
    "auth",
    "bearer",
    "credential",
    "credentials",
    "privatekey",
    "private_key",
    "sessionid",
    "session_id",
    "sessiontoken",
    "session_token"
  )
  
  /**
   * Normalizes a Map by filtering out sensitive fields recursively.
   * Also expands "body" fields that contain toString() output into individual fields.
   * 
   * @param data The map to normalize
   * @return A new map with sensitive fields replaced with [REDACTED_VALUE] and body expanded
   */
  fun normalizeMap(data: Map<String, Any?>): Map<String, Any?> {
    val normalized = mutableMapOf<String, Any?>()
    
    data.forEach { (key, value) ->
      // Special handling for "body" field - parse and expand it
      if (key == "body" && value is String) {
        val parsedBody = parseBodyString(value)
        if (parsedBody.isNotEmpty()) {
          // Add individual fields from parsed body
          parsedBody.forEach { (fieldKey, fieldValue) ->
            normalized[fieldKey] = normalizeValue(fieldKey, fieldValue)
          }
        } else {
          // If parsing failed, normalize the string as usual
          normalized[key] = normalizeValue(key, value)
        }
      } else {
        normalized[key] = normalizeValue(key, value)
      }
    }
    
    return normalized
  }
  
  /**
   * Normalizes a JSONObject by filtering out sensitive fields recursively.
   * 
   * @param jsonObject The JSONObject to normalize
   * @return A new JSONObject with sensitive fields replaced with [REDACTED_VALUE]
   */
  fun normalizeJsonObject(jsonObject: JSONObject): JSONObject {
    val normalized = JSONObject()
    jsonObject.keys().forEach { key ->
      try {
        val value = jsonObject.get(key)
        normalized.put(key, normalizeValue(key, value))
      } catch (e: Exception) {
        // If we can't get the value, skip it or put null
        normalized.put(key, null)
      }
    }
    return normalized
  }
  
  /**
   * Normalizes a value based on its key and type.
   * 
   * @param key The key name
   * @param value The value to normalize
   * @return The normalized value
   */
  @Suppress("UNCHECKED_CAST")
  private fun normalizeValue(key: String, value: Any?): Any? {
    if (value == null) {
      return null
    }
    
    // Check if the key indicates sensitive data
    val normalizedKey = key.lowercase().replace("_", "").replace("-", "")
    if (sensitiveFieldNames.contains(normalizedKey)) {
      return REDACTED_VALUE
    }
    
    // Handle nested maps
    if (value is Map<*, *>) {
      return normalizeMap(value as Map<String, Any?>)
    }
    
    // Handle JSONObject
    if (value is JSONObject) {
      return normalizeJsonObject(value)
    }
    
    // Handle lists/collections
    if (value is List<*>) {
      return value.map { item ->
        when (item) {
          is Map<*, *> -> normalizeMap(item as Map<String, Any?>)
          is JSONObject -> normalizeJsonObject(item)
          else -> item
        }
      }
    }
    
    // Handle strings that might contain sensitive data (e.g., "Bearer token123")
    if (value is String) {
      return normalizeString(key, value)
    }
    
    return value
  }
  
  /**
   * Normalizes a string value, checking if it contains sensitive patterns.
   * 
   * @param key The key name
   * @param value The string value
   * @return The normalized string
   */
  private fun normalizeString(key: String, value: String): String {
    val normalizedKey = key.lowercase().replace("_", "").replace("-", "")
    
    // Check if key suggests authorization header
    if (normalizedKey.contains("authorization") || normalizedKey.contains("auth")) {
      // If it looks like a Bearer token, Basic auth, or any token-like value, redact it
      if (value.startsWith("Bearer ", ignoreCase = true) || 
          value.startsWith("Basic ", ignoreCase = true) ||
          value.startsWith("Token ", ignoreCase = true)) {
        return REDACTED_VALUE
      }
      // Even if it doesn't start with Bearer/Basic, if the key is authorization/auth, redact it
      if (normalizedKey == "authorization" || normalizedKey == "auth") {
        return REDACTED_VALUE
      }
    }
    
    // Check if the value looks like a token (long alphanumeric string)
    // Only redact if the key suggests it's sensitive
    if (normalizedKey.contains("token") || normalizedKey.contains("key") || 
        normalizedKey.contains("secret") || normalizedKey.contains("password")) {
      // If it's a reasonably long string that looks like a token/key, redact it
      if (value.length > 10 && value.matches(Regex("^[A-Za-z0-9._-]+$"))) {
        return REDACTED_VALUE
      }
    }
    
    // Normalize base URLs in string values (e.g., in body strings)
    return normalizeUrlsInString(value)
  }
  
  /**
   * Normalizes URLs within a string by removing base URLs and keeping only paths.
   * 
   * Examples:
   * - "https://api.kksoft.vn/api/auth/login" -> "/api/auth/login"
   * - "Request to https://api-staging.kksoft.vn/api/payment" -> "Request to /api/payment"
   * - "Some text with https://api.kksoft.vn/path?param=value" -> "Some text with /path?param=value"
   * - "Body: https://api.kksoft.vn/api/auth/login?token=abc" -> "Body: /api/auth/login?token=abc"
   * 
   * @param value The string value that may contain URLs
   * @return The normalized string with base URLs removed
   */
  private fun normalizeUrlsInString(value: String): String {
    var normalized = value
    
    // Pattern to match full URLs (http:// or https:// followed by domain and optional path/query/fragment)
    // This captures the entire URL including path, query params, and fragments
    val fullUrlPattern = Regex("(https?://[^\\s\"'<>]+)", RegexOption.IGNORE_CASE)
    
    normalized = fullUrlPattern.replace(normalized) { matchResult ->
      val fullUrl = matchResult.value.trim()
      
      try {
        // Parse the URL to extract components
        val url = URL(fullUrl)
        val path = url.path
        val query = url.query
        val fragment = url.ref
        
        // Build the normalized path
        val pathPart = if (path.isNotEmpty()) path else "/"
        val queryPart = if (query != null) "?$query" else ""
        val fragmentPart = if (fragment != null) "#$fragment" else ""
        
        val normalizedPath = pathPart + queryPart + fragmentPart
        
        // If the normalized path is just "/", we might want to redact it entirely
        // But for now, keep it as "/" to indicate there was a URL
        normalizedPath
      } catch (e: Exception) {
        // If URL parsing fails, try manual extraction
        // Look for the path part after the domain
        val domainEndIndex = fullUrl.indexOf("/", 8) // Skip "https://"
        if (domainEndIndex > 0) {
          val pathAndQuery = fullUrl.substring(domainEndIndex)
          // Remove any trailing characters that aren't part of URL (like quotes, brackets)
          val cleanPath = pathAndQuery.takeWhile { 
            it.isLetterOrDigit() || it in "/?&#=:._-"
          }
          if (cleanPath.isNotEmpty()) {
            cleanPath
          } else {
            REDACTED_VALUE
          }
        } else {
          // No path found, just redact the domain
          REDACTED_VALUE
        }
      }
    }
    
    return normalized
  }
  
  /**
   * Normalizes an event name by removing base URLs and keeping only the path.
   * 
   * Examples:
   * - "https://api.kksoft.vn/api/auth/login" -> "/api/auth/login"
   * - "https://api-staging.kksoft.vn/api/payment/verify" -> "/api/payment/verify"
   * - "/api/auth/login" -> "/api/auth/login" (already normalized)
   * - "getGameInfo" -> "getGameInfo" (not a URL, keep as is)
   * 
   * @param eventName The event name (may be a full URL or path)
   * @return The normalized event name with base URL removed
   */
  fun normalizeEventName(eventName: String): String {
    if (eventName.isBlank()) {
      return eventName
    }
    
    // If it's already a path (starts with /), return as is
    if (eventName.startsWith("/")) {
      return eventName
    }
    
    // If it doesn't look like a URL (no http/https), return as is
    if (!eventName.startsWith("http://", ignoreCase = true) && 
        !eventName.startsWith("https://", ignoreCase = true)) {
      return eventName
    }
    
    // Try to parse as URL and extract path
    return try {
      val url = URL(eventName)
      val path = url.path
      val query = url.query
      
      // Return path with query string if present
      if (query != null) {
        "$path?$query"
      } else {
        path.ifEmpty { "/" }
      }
    } catch (e: Exception) {
      // If URL parsing fails, try to extract path manually
      extractPathFromUrl(eventName)
    }
  }
  
  /**
   * Extracts the path portion from a URL string manually.
   * 
   * @param urlString The URL string
   * @return The path portion or the original string if extraction fails
   */
  private fun extractPathFromUrl(urlString: String): String {
    // Remove protocol and domain
    val withoutProtocol = urlString
      .replace(Regex("^https?://"), "")
      .replace(Regex("^[^/]+"), "")
    
    // Extract path and query
    val pathAndQuery = if (withoutProtocol.startsWith("/")) {
      withoutProtocol
    } else {
      "/$withoutProtocol"
    }
    
    // Remove fragment if present
    val withoutFragment = pathAndQuery.split("#")[0]
    
    return withoutFragment.ifEmpty { "/" }
  }
  
  /**
   * Parses a toString() output string into a map of key-value pairs.
   * 
   * Handles formats like:
   * - "ClassName(field1=value1, field2=value2, field3=value3)"
   * - "ClassName(field1=value1, field2=value2)" (may be truncated)
   * 
   * @param bodyString The toString() output string
   * @return A map of parsed fields, or empty map if parsing fails
   */
  private fun parseBodyString(bodyString: String): Map<String, String> {
    if (bodyString.isBlank()) {
      return emptyMap()
    }
    
    val result = mutableMapOf<String, String>()
    
    try {
      // Pattern: ClassName(field1=value1, field2=value2, ...)
      // Extract content between parentheses
      val openParenIndex = bodyString.indexOf('(')
      if (openParenIndex < 0) {
        return emptyMap()
      }
      
      val content = bodyString.substring(openParenIndex + 1)
      // Remove trailing closing parenthesis if present (may be missing if truncated)
      val cleanContent = content.trimEnd(')', ' ', ',')
      
      if (cleanContent.isBlank()) {
        return emptyMap()
      }
      
      // Split by comma, but be careful with commas inside values
      // Simple approach: split by ", " (comma followed by space) which is the standard format
      val parts = cleanContent.split(", ")
      
      for (part in parts) {
        if (part.isBlank()) continue
        
        // Split by first "=" to get key and value
        val equalIndex = part.indexOf('=')
        if (equalIndex > 0) {
          val fieldKey = part.substring(0, equalIndex).trim()
          val fieldValue = part.substring(equalIndex + 1).trim()
          
          // Only add if both key and value are non-empty
          if (fieldKey.isNotEmpty() && fieldValue.isNotEmpty()) {
            result[fieldKey] = fieldValue
          }
        }
      }
    } catch (e: Exception) {
      // If parsing fails, return empty map (will fall back to normal string normalization)
      return emptyMap()
    }
    
    return result
  }
}
