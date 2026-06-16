package com.example.thehawkinslabyrinth

import android.content.Context

object LeaderboardManager {

    private const val PREF_NAME = "leaderboard_prefs"
    private const val KEY_SCORES = "scores"

    fun saveScore(
        context: Context,
        playerName: String,
        playerHP: Int,
        vecnaHP: Int
    ) {

        val score = playerHP - vecnaHP

        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val existing =
            prefs.getString(KEY_SCORES, "") ?: ""

        val entries = mutableListOf<LeaderboardEntry>()

        if (existing.isNotEmpty()) {
            existing.split(";").forEach { item ->

                val parts = item.split(",")

                if (parts.size == 2) {
                    entries.add(
                        LeaderboardEntry(
                            parts[0],
                            parts[1].toInt()
                        )
                    )
                }
            }
        }

        entries.add(
            LeaderboardEntry(
                playerName,
                score
            )
        )

        entries.sortByDescending { it.score }

        val top10 = entries.take(10)

        val saveString =
            top10.joinToString(";") {
                "${it.playerName},${it.score}"
            }

        prefs.edit()
            .putString(KEY_SCORES, saveString)
            .apply()
    }

    fun getScores(
        context: Context
    ): List<LeaderboardEntry> {

        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val saved =
            prefs.getString(KEY_SCORES, "") ?: ""

        if (saved.isEmpty()) return emptyList()

        return saved.split(";").mapNotNull { item ->

            val parts = item.split(",")

            if (parts.size == 2) {
                LeaderboardEntry(
                    parts[0],
                    parts[1].toInt()
                )
            } else null
        }
    }
}