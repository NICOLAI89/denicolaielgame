package com.nicolaielgame.game

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditsDocumentationTest {
    @Test
    fun creditsListBundledKenneyPacksAndLocalAssetFolders() {
        val credits = repoFile("CREDITS.md").readText()

        listOf(
            "Tower Defense",
            "UI Pack - Sci-Fi",
            "Interface Sounds",
            "Impact Sounds",
            "Digital Audio",
            "Sci-fi Sounds",
            "Creative Commons CC0",
            "app/src/main/assets",
        ).forEach { expected ->
            assertTrue("Expected CREDITS.md to mention $expected", expected in credits)
        }
    }

    private fun repoFile(path: String): File {
        return sequenceOf(File(path), File("..", path))
            .firstOrNull { it.exists() }
            ?: error("Could not find $path from ${File(".").absolutePath}")
    }
}
