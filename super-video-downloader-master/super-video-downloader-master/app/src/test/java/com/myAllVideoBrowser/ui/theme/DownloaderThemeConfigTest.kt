package com.myAllVideoBrowser.ui.theme

import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets
import java.util.stream.Stream
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloaderThemeConfigTest {

    @Test
    fun `downloader activities use unique theme names to avoid host collisions`() {
        val styles = readProjectFile("src/main/res/values/styles.xml")
        val manifest = readProjectFile("src/main/AndroidManifest.xml")

        assertTrue(
            "Splash theme should hand off to a downloader-specific app theme",
            styles.contains("""<item name="postSplashScreenTheme">@style/DownloaderAppTheme</item>""")
        )
        assertTrue(
            "Downloader app theme should use a unique resource name",
            styles.contains("""<style name="DownloaderAppTheme" parent="Base.DownloaderAppTheme">""")
        )
        assertTrue(
            "Video player activity should use the downloader-specific theme",
            manifest.contains("""android:theme="@style/DownloaderAppTheme"""")
        )
    }

    private fun readProjectFile(relativePath: String): String =
        String(Files.readAllBytes(findProjectFile(relativePath)), StandardCharsets.UTF_8)

    private fun findProjectFile(relativePath: String): Path {
        val normalized = relativePath.replace('\\', '/')
        val start = Path.of("").toAbsolutePath().normalize()

        generateSequence(start) { it.parent }.forEach { base ->
            val directMatch = base.resolve(normalized)
            if (Files.exists(directMatch)) {
                return directMatch
            }
        }

        Files.walk(start, 8).use { paths: Stream<Path> ->
            return paths
                .filter { candidate ->
                    candidate.toString().replace('\\', '/').endsWith(normalized)
                }
                .findFirst()
                .orElseThrow {
                    IllegalStateException("Could not find project file: $relativePath from $start")
                }
        }
    }
}
