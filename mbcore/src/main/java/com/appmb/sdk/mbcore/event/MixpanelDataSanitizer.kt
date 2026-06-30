package com.appmb.sdk.mbcore.event

import org.json.JSONObject

/**
 * Utility class to sanitize sensitive data before sending to Mixpanel.
 * Truncates sensitive fields such as passwords, access tokens, and refresh tokens.
 */
object MixpanelDataSanitizer {
  
  /**
   * List of sensitive field names that should be truncated.
   * Case-insensitive matching is used.
   */
  private val sensitiveFields = setOf(
    "password",
    "accessToken",
    "access_token",
    "refreshToken",
    "refresh_token",
    "purchaseToken",
    "purchase_token",
    "token",
    "authorization"
  )
  
  /**
   * Truncated value to replace sensitive data with.
   */
  private const val TRUNCATED_VALUE = "[REDACTED]"
  
  /**
   * Base URLs that should be removed from event names.
   * These are the API base URLs that shouldn't be sent to Mixpanel.
   */
  private val baseUrls = setOf(
    "https://api.kksoft.vn",
    "https://api-staging.kksoft.vn",
    "http://api.kksoft.vn",
    "http://api-staging.kksoft.vn"
  )
  
  /**
   * Sanitizes a Map by recursively truncating sensitive fields.
   * 
   * @param properties The map to sanitize
   * @return A new map with sensitive fields truncated
   */
  fun sanitizeMap(properties: Map<String, Any?>): Map<String, Any?> {
    return properties.mapValues { (key, value) ->
      sanitizeValue(key, value)
    }
  }
  
  /**
   * Sanitizes a JSONObject by recursively truncating sensitive fields.
   * 
   * @param jsonObject The JSONObject to sanitize
   * @return A new JSONObject with sensitive fields truncated
   */
  fun sanitizeJSONObject(jsonObject: JSONObject): JSONObject {
    val sanitized = JSONObject()
    val keys = jsonObject.keys()
    
    while (keys.hasNext()) {
      val key = keys.next()
      val value = jsonObject.get(key)
      val sanitizedValue = sanitizeValue(key, value)
      
      when (sanitizedValue) {
        is JSONObject -> sanitized.put(key, sanitizedValue)
        is Map<*, *> -> sanitized.put(key, JSONObject(sanitizedValue as Map<*, *>))
        is List<*> -> sanitized.put(key, sanitizedValue)
        null -> sanitized.put(key, JSONObject.NULL)
        else -> sanitized.put(key, sanitizedValue)
      }
    }
    
    return sanitized
  }
  
  /**
   * Sanitizes a value based on its key and type.
   * 
   * @param key The key name
   * @param value The value to sanitize
   * @return The sanitized value
   */
  private fun sanitizeValue(key: String, value: Any?): Any? {
    if (value == null) {
      return null
    }
    
    // Check if the key is sensitive (case-insensitive)
    val isSensitive = sensitiveFields.any { 
      key.equals(it, ignoreCase = true) || 
      key.contains(it, ignoreCase = true)
    }
    
    // If the key is sensitive and the value is a string, truncate it
    if (isSensitive && value is String) {
      return TRUNCATED_VALUE
    }
    
    // Handle nested maps
    if (value is Map<*, *>) {
      @Suppress("UNCHECKED_CAST")
      return sanitizeMap(value as Map<String, Any?>)
    }
    
    // Handle nested JSONObjects
    if (value is JSONObject) {
      return sanitizeJSONObject(value)
    }
    
    // Handle lists (which might contain maps or JSONObjects)
    if (value is List<*>) {
      return value.map { item ->
        when (item) {
          is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            sanitizeMap(item as Map<String, Any?>)
          }
          is JSONObject -> sanitizeJSONObject(item)
          else -> item
        }
      }
    }
    
    // Handle strings in Authorization headers
    if (key.equals("authorization", ignoreCase = true) && value is String) {
      return TRUNCATED_VALUE
    }
    
    // Handle strings that might contain sensitive data (e.g., "Bearer token")
    if (value is String && isSensitive) {
      return TRUNCATED_VALUE
    }
    
    // Handle string values that might contain serialized sensitive data
    // This covers cases like request.toString() which might contain password=xxx
    if (value is String && containsSensitiveData(value)) {
      return sanitizeString(value)
    }
    
    return value
  }
  
  /**
   * Checks if a string contains patterns that indicate sensitive data.
   * 
   * @param str The string to check
   * @return true if the string contains sensitive data patterns
   */
  private fun containsSensitiveData(str: String): Boolean {
    return sensitiveFields.any { field ->
      // Check for patterns like "password=xxx", "password:xxx", "accessToken=xxx", etc.
      val patterns = listOf(
        "$field\\s*[:=]\\s*[^\\s,}]+",
        "\"$field\"\\s*[:=]\\s*\"[^\"]+\"",
        "'$field'\\s*[:=]\\s*'[^']+'"
      )
      patterns.any { pattern ->
        Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(str)
      }
    } || Regex("bearer\\s+[^\\s,}]+", RegexOption.IGNORE_CASE).containsMatchIn(str)
  }
  
  /**
   * Sanitizes a string representation of an object (like request.toString()).
   * This is a fallback method for when we receive stringified objects.
   * 
   * @param str The string to sanitize
   * @return The sanitized string with sensitive data replaced
   */
  fun sanitizeString(str: String?): String {
    if (str.isNullOrEmpty()) {
      return str ?: ""
    }
    
    var sanitized = str
    
    // Pattern to match common sensitive field patterns in stringified objects
    // Pattern: password=value or password:value (with optional quotes)
    sanitized = Regex("""(password|accessToken|access_token|refreshToken|refresh_token|purchaseToken|purchase_token)\s*[:=]\s*["']?([^"',}\s]+)["']?""", RegexOption.IGNORE_CASE)
      .replace(sanitized) { matchResult ->
        val fieldName = matchResult.groupValues[1]
        "$fieldName=$TRUNCATED_VALUE"
      }
    
    // Pattern: "password": "value" (JSON format)
    sanitized = Regex("""("(?:password|accessToken|access_token|refreshToken|refresh_token|purchaseToken|purchase_token)"\s*:\s*)"([^"]+)"""", RegexOption.IGNORE_CASE)
      .replace(sanitized) { matchResult ->
        "${matchResult.groupValues[1]}\"$TRUNCATED_VALUE\""
      }
    
    // Pattern: Bearer token (Authorization header format)
    sanitized = Regex("""Bearer\s+([^\s"',}]+)""", RegexOption.IGNORE_CASE)
      .replace(sanitized) { "Bearer $TRUNCATED_VALUE" }
    
    return sanitized
  }
  
  /**
   * Sanitizes an event name by removing base URLs.
   * Keeps only the path portion of the URL.
   * 
   * Examples:
   * - "https://api.kksoft.vn/api/auth/login" -> "/api/auth/login"
   * - "https://api-staging.kksoft.vn/api/payment/verify" -> "/api/payment/verify"
   * - "/api/auth/login" -> "/api/auth/login" (unchanged if no base URL)
   * 
   * @param eventName The event name that may contain a base URL
   * @return The sanitized event name with base URL removed
   */
  fun sanitizeEventName(eventName: String?): String {
    if (eventName.isNullOrEmpty()) {
      return eventName ?: ""
    }
    
    var sanitized = eventName.trim()
    
    // Try to remove each known base URL first
    var baseUrlRemoved = false
    for (baseUrl in baseUrls) {
      if (sanitized.startsWith(baseUrl, ignoreCase = true)) {
        sanitized = sanitized.removePrefix(baseUrl)
        baseUrlRemoved = true
        break
      }
    }
    
    // If base URL was removed, ensure the path starts with /
    if (baseUrlRemoved) {
      if (sanitized.isEmpty()) {
        sanitized = "/"
      } else if (!sanitized.startsWith("/")) {
        sanitized = "/$sanitized"
      }
    } else {
      // If no known base URL was found, try to extract path from any URL pattern
      // This handles cases where the URL might not be in our baseUrls list
      try {
        val urlPattern = Regex("""https?://[^/]+(/.*)""", RegexOption.IGNORE_CASE)
        val match = urlPattern.find(sanitized)
        if (match != null) {
          sanitized = match.groupValues[1]
        }
      } catch (e: Exception) {
        // If parsing fails, return as-is
      }
    }
    
    // Remove query parameters and fragments for cleaner event names
    sanitized = sanitized.split("?")[0].split("#")[0]
    
    return sanitized
  }
}

