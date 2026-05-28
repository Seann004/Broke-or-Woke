package com.example.brokeorwoke.model

data class DialogueLine(
    val type: String = "dialogue",      // "dialogue", "narration", "system", "choice"
    val speaker: String = "",
    val character: String = "",         // "clara", "aiden", "evelyn", "mc"
    val expression: String = "neutral", // matches drawable suffix
    val text: String = ""
)

data class SceneChoice(
    val text: String = "",
    val effects: Map<String, Int> = emptyMap(),
    val flags: List<String> = emptyList(),
    val next: String = ""
)

data class DialogueScene(
    val id: String = "",
    val location: String = "",
    val background: String = "bg_bedroom_night",
    val lines: List<DialogueLine> = emptyList(),
    val choices: List<SceneChoice> = emptyList(),
    val next: String = ""
)