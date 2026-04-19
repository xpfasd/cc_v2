package com.myAllVideoBrowser.ui.main.home

internal fun shouldAutoRequestLauncherOnLaunch(
    isFirstStart: Boolean,
    isDefaultHome: Boolean
): Boolean = !isFirstStart && !isDefaultHome

internal fun shouldRequestLauncherBeforeGuide(
    isDefaultHome: Boolean
): Boolean = !isDefaultHome
