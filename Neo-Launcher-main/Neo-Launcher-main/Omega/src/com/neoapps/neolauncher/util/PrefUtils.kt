package com.neoapps.neolauncher.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import com.android.launcher3.R
import com.android.launcher3.qsb.OSEManager
import com.neoapps.neolauncher.groups.AppGroupsManager
import com.neoapps.neolauncher.preferences.PREFS_LANGUAGE_CODES
import com.neoapps.neolauncher.preferences.PREFS_LANGUAGE_SYSTEM_CODE
import com.neoapps.neolauncher.preferences.PREFS_LANGUAGE_SYSTEM_NAME
import java.util.Locale

fun Context.languageOptions(): Map<String, String> {
    return buildLanguageOptions(
        systemLocale = resources.configuration.locales[0],
        resolveLocale = { localeCode ->
            Config(this).getLocaleByAndroidCode(localeCode)
        }
    )
}

internal fun buildLanguageOptions(
    systemLocale: Locale,
    resolveLocale: (String) -> Locale = { localeCode -> localeFromAndroidCode(localeCode) },
): Map<String, String> {
    val entries = linkedMapOf<String, String>()
    entries[PREFS_LANGUAGE_SYSTEM_CODE] =
        "$PREFS_LANGUAGE_SYSTEM_NAME - ${summarizeLocale(systemLocale, PREFS_LANGUAGE_SYSTEM_CODE)}"

    for (languageCode in PREFS_LANGUAGE_CODES) {
        entries[languageCode] = summarizeLocale(resolveLocale(languageCode), languageCode)
    }

    return entries
}

internal fun localeFromAndroidCode(
    languageCode: String,
    fallbackLocale: Locale = Locale.getDefault(),
): Locale {
    if (languageCode.isEmpty()) {
        return fallbackLocale
    }
    return if (languageCode.contains("-r")) {
        Locale(languageCode.substring(0, 2), languageCode.substring(4, 6))
    } else {
        Locale(languageCode)
    }
}

internal fun summarizeLocale(locale: Locale, localeAndroidCode: String): String {
    val country = locale.getDisplayCountry(locale)
    val language = locale.getDisplayLanguage(locale)
    var ret = (locale.getDisplayLanguage(Locale.ENGLISH)
        .toString() + " (" + language.substring(0, 1)
        .uppercase(Locale.getDefault()) + language.substring(1)
            + (if (
                country.isNotEmpty() &&
                country.lowercase(Locale.getDefault()) != language.lowercase(Locale.getDefault())
            ) ", $country" else "")
            + ")")
    if (localeAndroidCode == "zh-rCN") {
        ret = ret.substring(0, ret.indexOf(" ") + 1) + "Simplified" + ret.substring(ret.indexOf(" "))
    } else if (localeAndroidCode == "zh-rTW") {
        ret = ret.substring(0, ret.indexOf(" ") + 1) + "Traditional" + ret.substring(ret.indexOf(" "))
    }
    return ret
}

fun Context.getFeedProviders(): Map<String, String> {
    val feeds = listOf(
        ProviderInfo(getString(R.string.none), "", getIcon())
    ) + availableFeedProviders().map {
        ProviderInfo(
            it.loadLabel(packageManager).toString(),
            it.packageName,
            it.loadIcon(packageManager)
        )
    }

    val entries = feeds.map { it.displayName }.toTypedArray()
    val entryValues = feeds.map { it.packageName }.toTypedArray()
    return entryValues.zip(entries).toMap()
}

val Context.drawerCategorizationOptions: Map<String, String>
    get() = listOfNotNull(
        AppGroupsManager.Category.NONE,
        AppGroupsManager.Category.FOLDER,
        AppGroupsManager.Category.TAB
    ).associate { Pair(it.key, getString(it.titleId)) }

fun Context.availableFeedProviders(): List<ApplicationInfo> {
    val packageManager = packageManager
    val intent = Intent(OSEManager.OVERLAY_ACTION)
        .setData(Uri.parse("app://$packageName"))
    val feedList: MutableList<ApplicationInfo> = ArrayList()
    for (resolveInfo in packageManager.queryIntentServices(
        intent,
        PackageManager.GET_RESOLVED_FILTER
    )) {
        if (resolveInfo.serviceInfo != null) {
            val applicationInfo = resolveInfo.serviceInfo.applicationInfo
            feedList.add(applicationInfo)
        }
    }
    return feedList
}

data class ProviderInfo(
    val displayName: String,
    val packageName: String,
    val icon: Drawable?,
)
