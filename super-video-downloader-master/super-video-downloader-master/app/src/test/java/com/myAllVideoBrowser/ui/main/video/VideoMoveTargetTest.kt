package com.myAllVideoBrowser.ui.main.video

import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class VideoMoveTargetTest {

    @Test
    fun `resolveMoveTarget keeps original name when destination is free`() {
        val targetDir = createTempDirectory("move-target-free").toFile()

        val target = resolveMoveTarget(targetDir, "tiktok_com.mp4")

        assertEquals(File(targetDir, "tiktok_com.mp4"), target)
    }

    @Test
    fun `resolveMoveTarget generates unique name when destination already exists`() {
        val targetDir = createTempDirectory("move-target-conflict").toFile()
        File(targetDir, "tiktok_com.mp4").writeText("existing")

        val target = resolveMoveTarget(targetDir, "tiktok_com.mp4")

        assertNotEquals(File(targetDir, "tiktok_com.mp4"), target)
        assertEquals(targetDir, target.parentFile)
    }
}
