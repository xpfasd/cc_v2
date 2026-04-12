package com.myAllVideoBrowser.ui.main.settings

import android.content.Context

internal fun requestLauncherSelection(
    context: Context,
    delegateFactory: (Context) -> LauncherActivationDelegate = ::AndroidLauncherActivationDelegate
) {
    LauncherActivationCoordinator(delegateFactory(context)).requestHomeSelection()
}
