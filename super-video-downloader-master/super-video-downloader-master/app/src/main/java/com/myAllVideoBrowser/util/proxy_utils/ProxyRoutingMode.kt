package com.myAllVideoBrowser.util.proxy_utils

object ProxyRoutingMode {
    fun shouldEnableLocalProxy(
        isProxyEnabled: Boolean,
        isDohEnabled: Boolean,
        isNativeProxyAvailable: Boolean = ProxyFeatureGate.isLocalProxyAvailable()
    ): Boolean {
        return isNativeProxyAvailable && (isProxyEnabled || isDohEnabled)
    }
}
