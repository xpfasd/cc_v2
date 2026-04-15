package com.myAllVideoBrowser.ui.main.settings

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

internal fun requestLauncherSelection(
    context: Context,
    roleRequestLauncher: ActivityResultLauncher<Intent>,
    delegateFactory: (Context, ActivityResultLauncher<Intent>) -> LauncherActivationDelegate =
        ::AndroidLauncherActivationDelegate
) {
    LauncherActivationCoordinator(delegateFactory(context, roleRequestLauncher)).requestHomeSelection()
}
