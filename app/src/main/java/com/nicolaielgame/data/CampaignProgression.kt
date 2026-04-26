package com.nicolaielgame.data

import com.nicolaielgame.game.model.LevelDefinition

enum class CampaignNodeState {
    Locked,
    Unlocked,
    Completed,
}

data class CampaignNode(
    val levelId: Int,
    val title: String,
    val state: CampaignNodeState,
    val bestScore: Int,
)

object CampaignProgression {
    fun nodes(
        levels: List<LevelDefinition>,
        highestUnlockedLevel: Int,
        bestScoresByLevel: Map<Int, Int>,
    ): List<CampaignNode> {
        return levels.map { level ->
            val bestScore = bestScoresByLevel[level.id] ?: 0
            CampaignNode(
                levelId = level.id,
                title = level.title,
                state = when {
                    bestScore > 0 -> CampaignNodeState.Completed
                    level.id <= highestUnlockedLevel -> CampaignNodeState.Unlocked
                    else -> CampaignNodeState.Locked
                },
                bestScore = bestScore,
            )
        }
    }

    fun nextPlayableLevelId(nodes: List<CampaignNode>): Int? {
        return nodes.firstOrNull { it.state == CampaignNodeState.Unlocked }?.levelId
            ?: nodes.lastOrNull { it.state == CampaignNodeState.Completed }?.levelId
    }
}
