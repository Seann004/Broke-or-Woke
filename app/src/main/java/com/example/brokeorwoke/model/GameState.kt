package com.example.brokeorwoke.model

data class GameState(
    var playerName: String = "",
    var balance: Int = 2500,
    var savings: Int = 0,
    var debt: Int = 0,
    var currentMonth: Int = 1,
    var currentSceneId: String = "s0_prologue",
    var currentActFile: String = "act1_scenes.json",  // tracks which act JSON to load
    var claraRel: Int = 0,
    var aidenRel: Int = 0,
    var evelynRel: Int = 0,
    var flags: MutableList<String> = mutableListOf()
)