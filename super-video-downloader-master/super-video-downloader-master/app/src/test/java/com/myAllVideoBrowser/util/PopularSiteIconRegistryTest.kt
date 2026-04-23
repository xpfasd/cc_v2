package com.myAllVideoBrowser.util

import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.PageInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PopularSiteIconRegistryTest {

    @Test
    fun `drawableResIdFor returns bundled icon for known site keys`() {
        assertEquals(R.drawable.ic_site_facebook, PopularSiteIconRegistry.drawableResIdFor("facebook"))
        assertEquals(R.drawable.ic_site_instagram, PopularSiteIconRegistry.drawableResIdFor("instagram"))
        assertEquals(R.drawable.ic_site_tiktok, PopularSiteIconRegistry.drawableResIdFor("tiktok"))
        assertEquals(R.drawable.ic_site_x, PopularSiteIconRegistry.drawableResIdFor("x"))
        assertEquals(R.drawable.ic_site_vimeo, PopularSiteIconRegistry.drawableResIdFor("vimeo"))
        assertEquals(R.drawable.ic_site_pinterest, PopularSiteIconRegistry.drawableResIdFor("pinterest"))
        assertEquals(R.drawable.ic_site_google, PopularSiteIconRegistry.drawableResIdFor("google"))
    }

    @Test
    fun `drawableResIdFor page uses bundled icon metadata`() {
        val pageInfo = PageInfo(
            name = "Google",
            link = "https://www.google.com",
            icon = "google"
        )

        assertEquals(R.drawable.ic_site_google, PopularSiteIconRegistry.drawableResIdFor(pageInfo))
    }

    @Test
    fun `shouldUseBundledIcon returns true for known bundled site`() {
        val pageInfo = PageInfo(
            name = "TikTok",
            link = "https://www.tiktok.com",
            icon = "tiktok"
        )

        assertTrue(PopularSiteIconRegistry.shouldUseBundledIcon(pageInfo))
    }

    @Test
    fun `unknown icon key keeps fallback behavior`() {
        val pageInfo = PageInfo(
            name = "Example",
            link = "https://example.com",
            icon = "example"
        )

        assertNull(PopularSiteIconRegistry.drawableResIdFor("example"))
        assertNull(PopularSiteIconRegistry.drawableResIdFor(pageInfo))
        assertFalse(PopularSiteIconRegistry.shouldUseBundledIcon(pageInfo))
    }
}
