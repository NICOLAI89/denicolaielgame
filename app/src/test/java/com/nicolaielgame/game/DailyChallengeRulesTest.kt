package com.nicolaielgame.game

import com.nicolaielgame.game.model.DailyChallengeRules
import com.nicolaielgame.game.model.EnemyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyChallengeRulesTest {
    @Test
    fun sameDateGeneratesSameChallenge() {
        val first = DailyChallengeRules.generate("2026-04-25")
        val second = DailyChallengeRules.generate("2026-04-25")

        assertEquals(first.seed, second.seed)
        assertEquals(first.baseLevelId, second.baseLevelId)
        assertEquals(first.difficulty, second.difficulty)
        assertEquals(first.modifiers, second.modifiers)
        assertEquals(first.level.waves.map { it.previewText() }, second.level.waves.map { it.previewText() })
    }

    @Test
    fun differentDatesUsuallyGenerateDifferentChallenges() {
        val first = DailyChallengeRules.generate("2026-04-25")
        val second = DailyChallengeRules.generate("2026-04-26")

        assertNotEquals(
            listOf(first.seed, first.baseLevelId, first.difficulty, first.modifiers),
            listOf(second.seed, second.baseLevelId, second.difficulty, second.modifiers),
        )
    }

    @Test
    fun dailyChallengeIncludesNewEnemyFamilies() {
        val challenge = DailyChallengeRules.generate("2026-04-25")
        val enemyTypes = challenge.level.waves.flatMap { wave -> wave.groups.map { it.type } }.toSet()

        assertTrue(EnemyType.Shielded in enemyTypes)
        assertTrue(EnemyType.Swarm in enemyTypes)
    }
}
