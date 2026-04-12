package com.myAllVideoBrowser.di.module.activity

import android.app.Activity
import com.myAllVideoBrowser.di.ActivityScoped
import com.myAllVideoBrowser.di.FragmentScoped
import com.myAllVideoBrowser.ui.main.settings.password.PasswordConfirmFragment
import com.myAllVideoBrowser.ui.main.settings.password.PasswordSetFragment
import com.myAllVideoBrowser.ui.main.settings.password.PasswordSuccessDialogFragment
import com.myAllVideoBrowser.ui.main.settings.password.SecurityQuestionDialogFragment
import com.myAllVideoBrowser.ui.main.settings.password.SecurityQuestionFragment
import com.myAllVideoBrowser.ui.main.video.PrivateSpaceActivity
import com.myAllVideoBrowser.ui.main.video.PrivateSpacePinFragment
import com.myAllVideoBrowser.ui.main.video.VideoFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PrivateSpaceModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindVideoFragment(): VideoFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindPrivateSpacePinFragment(): PrivateSpacePinFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindPasswordSetFragment(): PasswordSetFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindPasswordConfirmFragment(): PasswordConfirmFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindSecurityQuestionFragment(): SecurityQuestionFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindSecurityQuestionDialogFragment(): SecurityQuestionDialogFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun bindPasswordSuccessDialogFragment(): PasswordSuccessDialogFragment

    @ActivityScoped
    @Binds
    abstract fun bindPrivateSpaceActivity(privateSpaceActivity: PrivateSpaceActivity): Activity
}
