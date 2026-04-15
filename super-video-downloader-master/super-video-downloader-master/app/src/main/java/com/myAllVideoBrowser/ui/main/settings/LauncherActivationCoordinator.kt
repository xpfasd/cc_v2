package com.myAllVideoBrowser.ui.main.settings

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.edit

const val LAUNCHER_ACTIVATION_PREFS = "launcher_activation_prefs"
const val RETURN_TO_DOWNLOADER_AFTER_HOME_SELECTION = "return_to_downloader_after_home_selection"

interface LauncherActivationDelegate {
    fun markReturnToAppAfterHomeSelection()
    fun isHomeRoleRequestAvailable(): Boolean
    fun requestHomeRole()
    fun openHomeSettings()
}

class LauncherActivationCoordinator(
    private val delegate: LauncherActivationDelegate
) {
    fun requestHomeSelection() {
        delegate.markReturnToAppAfterHomeSelection()
        try {
            if (delegate.isHomeRoleRequestAvailable()) {
                delegate.requestHomeRole()
            } else {
                delegate.openHomeSettings()
            }
        } catch (_: Throwable) {
            delegate.openHomeSettings()
        }
    }
}

class AndroidLauncherActivationDelegate(
    private val context: Context,
    private val roleRequestLauncher: ActivityResultLauncher<Intent>
) : LauncherActivationDelegate {
    override fun markReturnToAppAfterHomeSelection() {
        context.getSharedPreferences(LAUNCHER_ACTIVATION_PREFS, Context.MODE_PRIVATE).edit {
            putBoolean(RETURN_TO_DOWNLOADER_AFTER_HOME_SELECTION, true)
        }
    }

    override fun isHomeRoleRequestAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false
        }

        val roleManager = context.getSystemService(RoleManager::class.java)
        return roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true
    }

    override fun requestHomeRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw IllegalStateException("ROLE_HOME is unavailable before Android Q")
        }

        val roleManager = context.getSystemService(RoleManager::class.java)
            ?: throw IllegalStateException("RoleManager unavailable")
        roleRequestLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
    }

    override fun openHomeSettings() {
        context.startActivity(
            Intent(Settings.ACTION_HOME_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
