/*
 * This file is part of Neo Launcher
 * Copyright (c) 2026   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.neoapps.neolauncher.allapps.comparator

import com.android.launcher3.model.data.AppInfo

class InstallTimeComparator(private val installTimes: Map<String, Long>) : Comparator<AppInfo> {
    override fun compare(app1: AppInfo, app2: AppInfo): Int = run {
        val app1InstallTime = installTimes[app1.componentName!!.packageName] ?: 0L
        val app2InstallTime = installTimes[app2.componentName!!.packageName] ?: 0L
        when {
            app1InstallTime < app2InstallTime -> 1
            app2InstallTime < app1InstallTime -> -1
            else -> 0
        }
    }
}
