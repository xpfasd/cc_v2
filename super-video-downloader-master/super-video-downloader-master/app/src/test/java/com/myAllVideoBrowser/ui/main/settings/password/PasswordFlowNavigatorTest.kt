package com.myAllVideoBrowser.ui.main.settings.password

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.video.VideoFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class PasswordFlowNavigatorTest {

    @Test
    fun `startChangePin opens reset pin flow without forcing private space reopen`() {
        val fragmentManager = mock(FragmentManager::class.java)
        val transaction = mock(FragmentTransaction::class.java)
        val fragmentCaptor = ArgumentCaptor.forClass(Fragment::class.java)
        `when`(fragmentManager.beginTransaction()).thenReturn(transaction)
        `when`(transaction.replace(eq(R.id.fragment_container_view), any(Fragment::class.java)))
            .thenReturn(transaction)
        `when`(transaction.addToBackStack(eq(PASSWORD_FLOW_START))).thenReturn(transaction)

        PasswordFlowNavigator.startChangePin(fragmentManager)

        verify(transaction).replace(eq(R.id.fragment_container_view), fragmentCaptor.capture())
        val fragment = fragmentCaptor.value as PasswordSetFragment
        val arguments = fragment.arguments
        assertTrue(arguments?.getBoolean("arg_reset_pin_only") == true)
        assertEquals(false, arguments?.getBoolean("arg_open_private_space_on_finish"))
    }

    @Test
    fun `finishToPrivateSpace clears stale back stack before opening private space`() {
        val fragmentManager = mock(FragmentManager::class.java)
        val transaction = mock(FragmentTransaction::class.java)
        val fragmentCaptor = ArgumentCaptor.forClass(Fragment::class.java)
        `when`(fragmentManager.beginTransaction()).thenReturn(transaction)
        `when`(transaction.replace(eq(R.id.fragment_container_view), any(Fragment::class.java)))
            .thenReturn(transaction)

        PasswordFlowNavigator.finishToPrivateSpace(fragmentManager)

        val inOrder = inOrder(fragmentManager, transaction)
        inOrder.verify(fragmentManager)
            .popBackStackImmediate(null as String?, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        inOrder.verify(fragmentManager).beginTransaction()
        inOrder.verify(transaction).replace(eq(R.id.fragment_container_view), fragmentCaptor.capture())
        assertTrue(fragmentCaptor.value is VideoFragment)
        verify(transaction).commit()
    }
}
