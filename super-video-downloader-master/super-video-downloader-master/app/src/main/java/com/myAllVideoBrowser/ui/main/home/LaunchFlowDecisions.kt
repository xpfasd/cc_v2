package com.myAllVideoBrowser.ui.main.home

internal fun shouldAutoRequestLauncherOnLaunch(
    isFirstStart: Boolean,
    isDefaultHome: Boolean
): Boolean = !isFirstStart && !isDefaultHome
