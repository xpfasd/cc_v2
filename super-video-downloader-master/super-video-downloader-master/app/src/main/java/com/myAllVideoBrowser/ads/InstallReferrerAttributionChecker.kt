package com.myAllVideoBrowser.ads

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.myAllVideoBrowser.util.AppLogger
import com.myAllVideoBrowser.util.SharedPrefHelper
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class InstallReferrerAttributionChecker(
    private val applicationContext: Context,
    private val sharedPrefHelper: SharedPrefHelper
) {
    suspend fun checkAndPersistIfNeeded() {
        val store = SharedPrefInstallReferrerAttributionStore(sharedPrefHelper)
        if (store.isAdAttributedUser()) {
            AppLogger.d("Install referrer attribution skipped because user is already marked attributed.")
            return
        }

        AppLogger.d("Install referrer attribution check started.")
        val rawReferrer = withTimeoutOrNull(5_000L) {
            fetchInstallReferrer()
        }

        if (rawReferrer == null) {
            sharedPrefHelper.markInstallReferrerAttributionAttempted()
            AppLogger.w("Install referrer attribution unavailable or timed out.")
            return
        }

        InstallReferrerAttributionCoordinator(store).recordReferrer(rawReferrer)
        if (store.isAdAttributedUser()) {
            AppLogger.i("Install referrer attribution marked current user as ad attributed.")
        } else {
            AppLogger.d("Install referrer attribution completed without tracked click identifiers.")
        }
    }

    private suspend fun fetchInstallReferrer(): String? = suspendCancellableCoroutine { continuation ->
        val client = InstallReferrerClient.newBuilder(applicationContext).build()

        continuation.invokeOnCancellation {
            runCatching { client.endConnection() }
        }

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                val referrer = when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        AppLogger.d("Install referrer setup finished successfully.")
                        runCatching { client.installReferrer.installReferrer }
                            .onFailure { error ->
                                AppLogger.w("Install referrer read failed: ${error.message}")
                            }
                            .getOrNull()
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        AppLogger.w("Install referrer feature is not supported on this device.")
                        null
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        AppLogger.w("Install referrer service is unavailable.")
                        null
                    }
                    else -> {
                        AppLogger.w("Install referrer setup finished with code=$responseCode.")
                        null
                    }
                }

                runCatching { client.endConnection() }
                if (continuation.isActive) {
                    continuation.resume(referrer)
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                AppLogger.w("Install referrer service disconnected before a result was received.")
                runCatching { client.endConnection() }
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        })
    }

    private class SharedPrefInstallReferrerAttributionStore(
        private val sharedPrefHelper: SharedPrefHelper
    ) : InstallReferrerAttributionStore {
        override fun isAdAttributedUser(): Boolean = sharedPrefHelper.getIsAdAttributedUser()

        override fun hasAttemptedInstallReferrerAttribution(): Boolean =
            sharedPrefHelper.hasAttemptedInstallReferrerAttribution()

        override fun getAdAttributionMatchedAtMillis(): Long =
            sharedPrefHelper.getAdAttributionMatchedAtMillis()

        override fun markInstallReferrerAttempted() {
            sharedPrefHelper.markInstallReferrerAttributionAttempted()
        }

        override fun markAdAttributedUser() {
            sharedPrefHelper.markAdAttributedUser()
        }

        override fun setAdAttributionMatchedAtMillis(matchedAtMillis: Long) {
            sharedPrefHelper.setAdAttributionMatchedAtMillis(matchedAtMillis)
        }
    }
}
