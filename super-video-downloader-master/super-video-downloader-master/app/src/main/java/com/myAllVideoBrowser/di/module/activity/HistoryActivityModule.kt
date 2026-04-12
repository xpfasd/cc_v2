package com.myAllVideoBrowser.di.module.activity

import com.myAllVideoBrowser.di.FragmentScoped
import com.myAllVideoBrowser.ui.main.history.HistoryFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HistoryActivityModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindHistoryFragment(): HistoryFragment
}
