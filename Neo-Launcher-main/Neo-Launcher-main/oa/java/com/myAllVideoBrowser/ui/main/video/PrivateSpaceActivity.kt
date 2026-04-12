package com.myAllVideoBrowser.ui.main.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.base.BaseActivity
import com.myAllVideoBrowser.ui.main.settings.password.PasswordSetFragment
import com.myAllVideoBrowser.util.SharedPrefHelper
import javax.inject.Inject

class PrivateSpaceActivity : BaseActivity() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_space)

        if (savedInstanceState != null) {
            return
        }

        val startFragment = when (
            PrivateSpaceStartDestinationResolver.resolve(sharedPrefHelper.hasPasswordPin())
        ) {
            PrivateSpaceStartDestination.AUTH -> PrivateSpacePinFragment.newInstance()
            PrivateSpaceStartDestination.PASSWORD_SETUP ->
                PasswordSetFragment.newInstance(openPrivateSpaceOnFinish = true)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, startFragment)
            .commit()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, PrivateSpaceActivity::class.java)
        }
    }
}
