package com.myAllVideoBrowser.di.module.activity

import com.myAllVideoBrowser.di.FragmentScoped
import com.myAllVideoBrowser.ui.main.bookmarks.BookmarksFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BookmarksActivityModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindBookmarksFragment(): BookmarksFragment
}
