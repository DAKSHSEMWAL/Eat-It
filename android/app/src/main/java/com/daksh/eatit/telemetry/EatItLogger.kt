package com.daksh.eatit.telemetry

import android.util.Log

object EatItLogger {
    private const val TAG = "EatItTelemetry"

    fun redactPhone(phone: String): String {
        val trimmed = phone.trim()
        if (trimmed.length < 5) return "***"
        val firstTwo = trimmed.take(2)
        val lastTwo = trimmed.takeLast(2)
        val masked = "*".repeat((trimmed.length - 4).coerceAtLeast(3))
        return "$firstTwo$masked$lastTwo"
    }

    fun redactEmail(email: String): String {
        val parts = email.trim().split("@")
        if (parts.size != 2 || parts[0].isEmpty()) return "***@***.com"
        val name = parts[0]
        val domain = parts[1]
        val maskedName = if (name.length <= 2) "${name.take(1)}***" else "${name.take(1)}***${name.takeLast(1)}"
        return "$maskedName@$domain"
    }

    fun redactAddress(address: String): String {
        val words = address.trim().split(Regex("\\s+"))
        if (words.isEmpty()) return "***"
        return words.joinToString(" ") { word ->
            if (word.length <= 2) word
            else "${word.first()}***${word.last()}"
        }
    }

    fun redactName(name: String): String {
        val parts = name.trim().split(Regex("\\s+"))
        if (parts.isEmpty()) return "***"
        return parts.joinToString(" ") { part ->
            if (part.length <= 1) part
            else "${part.first()}***"
        }
    }

    fun sanitizeValue(key: String, value: String): String {
        val k = key.lowercase()
        return when {
            k.contains("phone") -> redactPhone(value)
            k.contains("email") -> redactEmail(value)
            k.contains("address") -> redactAddress(value)
            k.contains("name") -> redactName(value)
            else -> value
        }
    }

    fun logCheckoutStage(stage: String, details: Map<String, String> = emptyMap()) {
        val sanitized = details.mapValues { (k, v) -> sanitizeValue(k, v) }
        val message = "CheckoutStage: $stage | $sanitized"
        try {
            Log.i(TAG, message)
        } catch (_: Exception) {
            println("[$TAG] $message")
        }
    }

    fun logError(stage: String, error: String) {
        val message = "CheckoutError: $stage | Error: $error"
        try {
            Log.e(TAG, message)
        } catch (_: Exception) {
            println("[$TAG] $message")
        }
    }
}
