package com.example.thehawkinslabyrinth

data class GameState(
    val row: Int,
    val col: Int,
    val hp: Int,
    val immunity: Int,
    val psycho: Boolean
)