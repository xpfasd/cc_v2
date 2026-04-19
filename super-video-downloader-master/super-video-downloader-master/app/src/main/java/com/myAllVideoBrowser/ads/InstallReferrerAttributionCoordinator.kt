package com.myAllVideoBrowser.ads

interface InstallReferrerAttributionStore {
    fun isAdAttributedUser(): Boolean
    fun hasAttemptedInstallReferrerAttribution(): Boolean
    fun getAdAttributionMatchedAtMillis(): Long
    fun markInstallReferrerAttempted()
    fun markAdAttributedUser()
    fun setAdAttributionMatchedAtMillis(matchedAtMillis: Long)
}

class InstallReferrerAttributionCoordinator(
    private val store: InstallReferrerAttributionStore,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    fun recordReferrer(rawReferrer: String?) {
        if (store.isAdAttributedUser()) {
            return
        }

        store.markInstallReferrerAttempted()
        if (InstallReferrerAttributionDecider.isAdAttributed(rawReferrer)) {
            store.markAdAttributedUser()
            store.setAdAttributionMatchedAtMillis(currentTimeMillis())
        }
    }
}
