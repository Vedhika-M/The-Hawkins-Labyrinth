package com.example.thehawkinslabyrinth

data class Move(
    val row: Int,
    val col: Int,
    val hp: Int,
    val diceRoll: Int,
    val isNormal: Boolean
)