package com.gallery.thekami.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppInfoTest {
    @Test
    fun applicationIdIsStable() {
        assertEquals("com.gallery.thekami", AppInfo.APPLICATION_ID)
    }
}
