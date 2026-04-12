package com.myAllVideoBrowser.di.module.activity

import com.myAllVideoBrowser.ui.main.home.browser.tabsOverview.TabsOverviewFragment
import org.junit.Assert.assertTrue
import org.junit.Test

class MainModuleTabsOverviewBindingTest {

    @Test
    fun `main module exposes tabs overview fragment binding`() {
        val hasBinding = MainModule::class.java.methods.any { method ->
            method.name == "bindTabsOverviewFragment" &&
                method.returnType == TabsOverviewFragment::class.java
        }

        assertTrue("Expected MainModule to bind TabsOverviewFragment", hasBinding)
    }
}
