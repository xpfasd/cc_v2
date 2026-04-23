package com.myAllVideoBrowser.util

import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.PageInfo

object PopularSiteIconRegistry {
    const val FACEBOOK = "facebook"
    const val INSTAGRAM = "instagram"
    const val TIKTOK = "tiktok"
    const val X = "x"
    const val VIMEO = "vimeo"
    const val PINTEREST = "pinterest"
    const val GOOGLE = "google"

    @JvmStatic
    fun drawableResIdFor(iconKey: String?): Int? {
        return when (iconKey?.trim()?.lowercase()) {
            FACEBOOK -> R.drawable.ic_site_facebook
            INSTAGRAM -> R.drawable.ic_site_instagram
            TIKTOK -> R.drawable.ic_site_tiktok
            X -> R.drawable.ic_site_x
            VIMEO -> R.drawable.ic_site_vimeo
            PINTEREST -> R.drawable.ic_site_pinterest
            GOOGLE -> R.drawable.ic_site_google
            else -> null
        }
    }

    @JvmStatic
    fun drawableResIdFor(pageInfo: PageInfo): Int? = drawableResIdFor(pageInfo.icon)

    @JvmStatic
    fun shouldUseBundledIcon(pageInfo: PageInfo): Boolean = drawableResIdFor(pageInfo) != null
}
