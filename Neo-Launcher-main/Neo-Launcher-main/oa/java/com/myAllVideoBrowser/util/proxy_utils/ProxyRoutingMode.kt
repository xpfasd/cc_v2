package com.myAllVideoBrowser.util.proxy_utils

object ProxyRoutingMode {
    fun shouldEnableLocalProxy(
        isProxyEnabled: Boolean,
        isDohEnabled: Boolean
    ): Boolean {
        return isProxyEnabled || isDohEnabled
    }
}
