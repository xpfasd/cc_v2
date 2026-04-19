package com.myAllVideoBrowser.ads

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object InstallReferrerAttributionDecider {
    private val trackedKeys = setOf("gclid", "gbraid", "wbraid", "fbclid", "ttclid")

    fun isAdAttributed(rawReferrer: String?): Boolean {
        if (rawReferrer.isNullOrBlank()) {
            return false
        }

        return rawReferrer
            .split("&")
            .asSequence()
            .mapNotNull { token ->
                val separatorIndex = token.indexOf('=')
                if (separatorIndex <= 0) {
                    return@mapNotNull null
                }
                val key = decode(token.substring(0, separatorIndex))
                val value = decode(token.substring(separatorIndex + 1))
                key to value
            }
            .any { (key, value) ->
                key in trackedKeys && value.isNotBlank()
            }
    }

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
