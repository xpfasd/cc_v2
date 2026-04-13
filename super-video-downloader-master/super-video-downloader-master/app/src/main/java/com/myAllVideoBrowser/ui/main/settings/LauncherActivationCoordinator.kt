package com.myAllVideoBrowser.ui.main.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.edit

const val NEO_FAKE_LAUNCHER_CLASS_NAME = "com.neoapps.neolauncher.FakeLauncher"
const val LAUNCHER_ACTIVATION_PREFS = "launcher_activation_prefs"
const val RETURN_TO_DOWNLOADER_AFTER_HOME_SELECTION = "return_to_downloader_after_home_selection"

interface LauncherActivationDelegate {
    fun enableFakeLauncher(className: String)
    fun markReturnToAppAfterHomeSelection()
    fun openHomePicker()
    fun restoreFakeLauncher(className: String)
    fun openHomeSettings()
}

class LauncherActivationCoordinator(
    private val delegate: LauncherActivationDelegate,
    private val fakeLauncherClassName: String = NEO_FAKE_LAUNCHER_CLASS_NAME
) {
    fun requestHomeSelection() {
        try {
            delegate.enableFakeLauncher(fakeLauncherClassName)
            delegate.markReturnToAppAfterHomeSelection()
            delegate.openHomePicker()
        } catch (_: Throwable) {
            delegate.openHomeSettings()
        } finally {
            delegate.restoreFakeLauncher(fakeLauncherClassName)
        }
    }
}

class AndroidLauncherActivationDelegate(
    private val context: Context
) : LauncherActivationDelegate {
    private val packageManager: PackageManager = context.packageManager

    override fun enableFakeLauncher(className: String) {
        packageManager.setComponentEnabledSetting(
            ComponentName(context.packageName, className),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun openHomePicker() {
        context.startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override fun markReturnToAppAfterHomeSelection() {
        context.getSharedPreferences(LAUNCHER_ACTIVATION_PREFS, Context.MODE_PRIVATE).edit {
            putBoolean(RETURN_TO_DOWNLOADER_AFTER_HOME_SELECTION, true)
        }
    }

    override fun restoreFakeLauncher(className: String) {
        packageManager.setComponentEnabledSetting(
            ComponentName(context.packageName, className),
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun openHomeSettings() {
        context.startActivity(
            Intent(Settings.ACTION_HOME_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
