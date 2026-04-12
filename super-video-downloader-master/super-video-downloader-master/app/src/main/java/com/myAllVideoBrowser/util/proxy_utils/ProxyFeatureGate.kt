package com.myAllVideoBrowser.util.proxy_utils

import com.myAllVideoBrowser.v2ray.V2Ray

object ProxyFeatureGate {
    const val LOCAL_PROXY_ENABLED = false

    @JvmStatic
    fun isLocalProxyAvailable(): Boolean {
        return LOCAL_PROXY_ENABLED && V2Ray.isNativeReady()
    }
}
