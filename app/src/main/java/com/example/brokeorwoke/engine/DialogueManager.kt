package com.example.brokeorwoke.engine

import android.content.Context
import com.example.brokeorwoke.model.DialogueScene
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DialogueManager(private val context: Context) {

    private var scenes: List<DialogueScene> = emptyList()
    private var currentSceneIndex: Int = 0
    private var currentLineIndex: Int = 0

    // Load all scenes from a JSON file in assets/dialogue/
    fun loadAct(fileName: String) {
        try {
            val json = context.assets.open("dialogue/$fileName")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<List<DialogueScene>>() {}.type
            scenes = Gson().fromJson(json, type)
            currentLineIndex = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Jump to a specific scene by ID
    fun goToScene(sceneId: String) {
        val index = scenes.indexOfFirst { it.id == sceneId }
        if (index != -1) {
            currentSceneIndex = index
            currentLineIndex = 0
        }
    }

    // Get the current scene
    fun getCurrentScene(): DialogueScene? {
        return scenes.getOrNull(currentSceneIndex)
    }

    // Get the current line within the current scene
    fun getCurrentLine() = getCurrentScene()?.lines?.getOrNull(currentLineIndex)

    // Check if there are more lines in the current scene
    fun hasNextLine(): Boolean {
        val scene = getCurrentScene() ?: return false
        return currentLineIndex < scene.lines.size - 1
    }

    // Check if current scene ends with choices
    fun hasChoices(): Boolean {
        val scene = getCurrentScene() ?: return false
        return currentLineIndex >= scene.lines.size - 1 && scene.choices.isNotEmpty()
    }

    // Advance to next line
    fun advanceLine() {
        if (hasNextLine()) {
            currentLineIndex++
        }
    }

    // Apply choice effects to GameState and navigate to next scene
    fun applyChoice(choiceIndex: Int, gameState: GameState): GameState {
        val scene = getCurrentScene() ?: return gameState
        val choice = scene.choices.getOrNull(choiceIndex) ?: return gameState

        // Apply numeric effects
        choice.effects.forEach { (key, value) ->
            when (key) {
                "clara_rel"  -> gameState.claraRel  = (gameState.claraRel  + value).coerceIn(0, 100)
                "aiden_rel"  -> gameState.aidenRel  = (gameState.aidenRel  + value).coerceIn(0, 100)
                "evelyn_rel" -> gameState.evelynRel = (gameState.evelynRel + value).coerceIn(0, 100)
                "balance"    -> gameState.balance   = (gameState.balance   + value).coerceAtLeast(0)
                "savings"    -> gameState.savings   = (gameState.savings   + value).coerceAtLeast(0)
                "debt"       -> gameState.debt      = (gameState.debt      + value).coerceAtLeast(0)
            }
        }

        // Apply flags
        choice.flags.forEach { flag ->
            if (!gameState.flags.contains(flag)) {
                gameState.flags.add(flag)
            }
        }

        // Navigate to next scene
        val nextId = choice.next.ifEmpty { scene.next }
        if (nextId.isNotEmpty()) {
            goToScene(nextId)
        }

        return gameState
    }

    // Get drawable resource name for a character + expression
    fun getCharacterDrawable(character: String, expression: String): String {
        return when (character) {
            "clara"  -> "char_clara_${expression}"
            "aiden"  -> "char_aiden_${expression}"
            "evelyn" -> "char_evelyn_${expression}"
            else     -> ""
        }
    }

    // Get background drawable name for current scene
    fun getCurrentBackground(): String {
        return getCurrentScene()?.background ?: "bg_bedroom_night"
    }

    fun isFinished(): Boolean {
        val scene = getCurrentScene() ?: return true
        val atLastLine = currentLineIndex >= scene.lines.size - 1
        val noChoices = scene.choices.isEmpty()
        val noNext = scene.next.isEmpty() || scene.next == "budget_screen"
        return atLastLine && noChoices && noNext
    }
}