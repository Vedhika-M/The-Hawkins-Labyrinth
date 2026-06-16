package com.example.thehawkinslabyrinth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class Cell(
    val unlockRequirement: Int,
    val isUpsideDown: Boolean,
    var isUnlocked: MutableState<Boolean> = mutableStateOf(false)
)