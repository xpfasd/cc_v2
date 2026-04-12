package com.myAllVideoBrowser.ui.main.progress

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressEventContractTest {

    @Test
    fun `parse failed event remains nullable because call emits no payload`() {
        val progressViewModel = readProjectFile(
            "src/main/java/com/myAllVideoBrowser/ui/main/progress/ProgressViewModel.kt"
        )

        assertTrue(
            "parseFailedEvent should stay nullable because SingleLiveEvent.call() emits a null payload",
            progressViewModel.contains("val parseFailedEvent = SingleLiveEvent<Unit?>()")
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
