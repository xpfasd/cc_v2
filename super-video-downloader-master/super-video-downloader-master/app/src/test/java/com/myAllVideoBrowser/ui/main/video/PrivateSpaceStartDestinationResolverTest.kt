package com.myAllVideoBrowser.ui.main.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateSpaceStartDestinationResolverTest {

    @Test
    fun `resolve returns auth when pin already exists`() {
        assertEquals(
            PrivateSpaceStartDestination.AUTH,
            PrivateSpaceStartDestinationResolver.resolve(hasPasswordPin = true)
        )
    }

    @Test
    fun `resolve returns password setup when pin does not exist`() {
        assertEquals(
            PrivateSpaceStartDestination.PASSWORD_SETUP,
            PrivateSpaceStartDestinationResolver.resolve(hasPasswordPin = false)
        )
    }

    @Test
    fun `canRecoverPassword returns true only when both question and answer exist`() {
        assertTrue(
            PrivateSpaceStartDestinationResolver.canRecoverPassword(
                hasSecurityQuestion = true,
                hasSecurityAnswer = true
            )
        )
        assertFalse(
            PrivateSpaceStartDestinationResolver.canRecoverPassword(
                hasSecurityQuestion = false,
                hasSecurityAnswer = true
            )
        )
        assertFalse(
            PrivateSpaceStartDestinationResolver.canRecoverPassword(
                hasSecurityQuestion = true,
                hasSecurityAnswer = false
            )
        )
    }
}
