package com.myAllVideoBrowser.di.module

import com.myAllVideoBrowser.di.ActivityScoped
import com.myAllVideoBrowser.ui.main.guide.GuideActivity
import com.myAllVideoBrowser.ui.main.bookmarks.BookmarksActivity
import com.myAllVideoBrowser.ui.main.history.HistoryActivity
import com.myAllVideoBrowser.ui.main.home.MainActivity
import com.myAllVideoBrowser.ui.main.settings.PrivacyPolicyActivity
import com.myAllVideoBrowser.ui.main.settings.language.LanguageSettingsActivity
import com.myAllVideoBrowser.di.module.activity.BookmarksActivityModule
import com.myAllVideoBrowser.di.module.activity.HistoryActivityModule
import com.myAllVideoBrowser.di.module.activity.MainModule
import com.myAllVideoBrowser.di.module.activity.PrivateSpaceModule
import com.myAllVideoBrowser.ui.main.player.VideoPlayerActivity
import com.myAllVideoBrowser.di.module.activity.VideoPlayerModule
import com.myAllVideoBrowser.ui.main.splash.SplashActivity
import com.myAllVideoBrowser.ui.main.video.PrivateSpaceActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector
    internal abstract fun bindSplashActivity(): SplashActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [MainModule::class])
    internal abstract fun bindMainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector
    internal abstract fun bindLanguageSettingsActivity(): LanguageSettingsActivity

    @ActivityScoped
    @ContributesAndroidInjector
    internal abstract fun bindPrivacyPolicyActivity(): PrivacyPolicyActivity

    @ActivityScoped
    @ContributesAndroidInjector
    internal abstract fun bindGuideActivity(): GuideActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [BookmarksActivityModule::class])
    internal abstract fun bindBookmarksActivity(): BookmarksActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [HistoryActivityModule::class])
    internal abstract fun bindHistoryActivity(): HistoryActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [VideoPlayerModule::class])
    internal abstract fun bindVideoPlayerActivity(): VideoPlayerActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [PrivateSpaceModule::class])
    internal abstract fun bindPrivateSpaceActivity(): PrivateSpaceActivity
}
