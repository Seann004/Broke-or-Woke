package com.example.brokeorwoke

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.brokeorwoke.databinding.ActivityDialogueBinding
import com.example.brokeorwoke.engine.DialogueManager
import com.example.brokeorwoke.model.DialogueLine
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson

class DialogueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDialogueBinding
    private lateinit var dialogueManager: DialogueManager
    private lateinit var gameState: GameState

    private val handler = Handler(Looper.getMainLooper())
    private var isTypewriting = false
    private var fullText = ""
    private var charIndex = 0
    private var canAdvance = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        gameState = loadGameState()
        dialogueManager = DialogueManager(this)
        dialogueManager.loadAct(gameState.currentActFile)
        dialogueManager.goToScene(gameState.currentSceneId)

        if (dialogueManager.getCurrentLine() == null) {
            dialogueManager.goToScene(
                when (gameState.currentActFile) {
                    "act2_scenes.json" -> "act2_intro"
                    "act3_scenes.json" -> "act3_intro"
                    else -> "s0_prologue"
                }
            )
        }

        updateHUD()
        displayCurrentLine()

        binding.dialogueBox.setOnClickListener {
            onDialogueTapped()
        }
    }

    private fun checkAndShowBio(character: String) {
        val flag = "met_$character"
        if (gameState.flags.contains(flag)) return
        if (character.isEmpty() || character == "mc") return

        val name: String
        val age: String
        val personality: String
        val tagline: String
        val color: String

        when (character) {
            "clara" -> {
                name = "Clara Lim"
                age = "27  ·  Finance Executive  ·  Same company, 2nd floor"
                personality = "Calm, dry humour, no-nonsense warmth.\nWent broke at 23. Rebuilt from zero."
                tagline = "\"I just wish someone had asked me this when I started.\""
                color = "#1D9E75"
            }
            "aiden" -> {
                name = "Aiden"
                age = "24  ·  Different company  ·  Your roommate"
                personality = "Loud, generous, zero impulse control.\nSpends full salary by week 2."
                tagline = "\"Life's too short to budget. Or so he keeps saying.\""
                color = "#D85A30"
            }
            "evelyn" -> {
                name = "Evelyn"
                age = "23  ·  Different company  ·  Childhood friend"
                personality = "Thoughtful, anxious, deeply loyal.\nLearning money stuff from scratch — same as you."
                tagline = "\"Can we just figure this out together?\""
                color = "#7F77DD"
            }
            else -> return
        }

        // Set bio content
        binding.tvBioName.text = name
        binding.tvBioName.setTextColor(android.graphics.Color.parseColor(color))
        binding.tvBioAge.text = age
        binding.tvBioPersonality.text = personality
        binding.tvBioTagline.text = tagline

        // Fade + scale in
        binding.bioOverlay.visibility = View.VISIBLE
        binding.bioCard.alpha = 0f
        binding.bioCard.scaleX = 0.9f
        binding.bioCard.scaleY = 0.9f

        ObjectAnimator.ofFloat(binding.bioCard, "alpha", 0f, 1f).apply { duration = 300; start() }
        ObjectAnimator.ofFloat(binding.bioCard, "scaleX", 0.9f, 1f).apply { duration = 300; start() }
        ObjectAnimator.ofFloat(binding.bioCard, "scaleY", 0.9f, 1f).apply { duration = 300; start() }

        // Dismiss
        binding.btnBioDismiss.setOnClickListener {
            ObjectAnimator.ofFloat(binding.bioCard, "alpha", 1f, 0f).apply {
                duration = 200
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.bioOverlay.visibility = View.GONE
                    }
                })
                start()
            }
            gameState.flags.add(flag)
            saveGameState(gameState)
        }
    }

    private fun onDialogueTapped() {
        if (binding.bioOverlay.visibility == View.VISIBLE) return
        when {
            isTypewriting -> {
                handler.removeCallbacksAndMessages(null)
                isTypewriting = false
                binding.tvDialogueText.text = fullText
                showTapHint()
                canAdvance = true
            }
            canAdvance && dialogueManager.hasNextLine() -> {
                dialogueManager.advanceLine()
                displayCurrentLine()
            }
            canAdvance && dialogueManager.hasChoices() -> {
                showChoices()
            }
            canAdvance && !dialogueManager.getCurrentScene()?.next.isNullOrEmpty() -> {
                val nextScene = dialogueManager.getCurrentScene()?.next ?: ""
                if (nextScene == "budget_screen") {
                    handleSceneEnd()
                } else {
                    dialogueManager.goToScene(nextScene)
                    displayCurrentLine()
                    updateBackground()
                    updateHUD()
                }
            }
            canAdvance -> {
                handleSceneEnd()
            }
        }
    }

    private fun displayCurrentLine() {
        val line = dialogueManager.getCurrentLine() ?: return
        canAdvance = false

        binding.choicesContainer.visibility = View.GONE
        binding.tvTapHint.visibility = View.GONE
        binding.choicesContainer.removeAllViews()

        when (line.type) {
            "narration" -> displayNarration(line)
            "system"    -> displaySystem(line)
            "dialogue"  -> displayDialogue(line)
            else        -> displayDialogue(line)
        }

        updateBackground()
    }

    private fun displayDialogue(line: DialogueLine) {
        if (line.speaker.isNotEmpty()) {
            binding.tvSpeakerName.visibility = View.VISIBLE
            binding.tvSpeakerName.text = line.speaker
            binding.tvSpeakerName.setTextColor(getSpeakerColor(line.character))
        } else {
            binding.tvSpeakerName.visibility = View.GONE
        }

        showCharacter(line.character, line.expression)
        checkAndShowBio(line.character)
        startTypewriter(line.text)
    }

    private fun displayNarration(line: DialogueLine) {
        binding.tvSpeakerName.visibility = View.GONE
        binding.imgCharacter.visibility = View.INVISIBLE
        startTypewriter(line.text, isNarration = true)
    }

    private fun displaySystem(line: DialogueLine) {
        binding.tvSpeakerName.visibility = View.GONE
        binding.imgCharacter.visibility = View.INVISIBLE
        startTypewriter(line.text, isSystem = true)
    }

    private fun startTypewriter(text: String, isNarration: Boolean = false, isSystem: Boolean = false) {
        fullText = text
        charIndex = 0
        isTypewriting = true
        binding.tvDialogueText.text = ""

        binding.tvDialogueText.setTextColor(
            when {
                isNarration -> Color.parseColor("#CCFFFFFF")
                isSystem    -> Color.parseColor("#EF9F27")
                else        -> Color.WHITE
            }
        )
        binding.tvDialogueText.textSize = if (isNarration) 12f else 13f
        typeNextChar()
    }

    private fun typeNextChar() {
        if (charIndex < fullText.length) {
            binding.tvDialogueText.text = fullText.substring(0, charIndex + 1)
            charIndex++
            handler.postDelayed({ typeNextChar() }, 30)
        } else {
            isTypewriting = false
            showTapHint()
            canAdvance = true
        }
    }

    private fun showTapHint() {
        if (!dialogueManager.hasChoices()) {
            binding.tvTapHint.visibility = View.VISIBLE
        }
    }

    private fun showChoices() {
        val scene = dialogueManager.getCurrentScene() ?: return
        binding.choicesContainer.removeAllViews()
        binding.choicesContainer.visibility = View.VISIBLE
        binding.tvTapHint.visibility = View.GONE
        canAdvance = false

        scene.choices.forEachIndexed { index, choice ->
            val choiceBtn = TextView(this).apply {
                text = choice.text
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(32, 20, 32, 20)
                background = ContextCompat.getDrawable(context, R.drawable.dialogue_bubble_bg)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
                setOnClickListener {
                    gameState = dialogueManager.applyChoice(index, gameState)
                    saveGameState(gameState)
                    updateHUD()
                    binding.choicesContainer.visibility = View.GONE
                    displayCurrentLine()
                }
            }
            binding.choicesContainer.addView(choiceBtn)
        }
    }

    private fun showCharacter(character: String, expression: String) {
        if (character.isEmpty() || character == "mc") {
            binding.imgCharacter.visibility = View.INVISIBLE
            return
        }
        val drawableName = dialogueManager.getCharacterDrawable(character, expression)
        val resId = resources.getIdentifier(drawableName, "drawable", packageName)
        if (resId != 0) {
            binding.imgCharacter.setImageResource(resId)
            binding.imgCharacter.visibility = View.VISIBLE
        } else {
            val fallback = dialogueManager.getCharacterDrawable(character, "neutral")
            val fallbackId = resources.getIdentifier(fallback, "drawable", packageName)
            if (fallbackId != 0) {
                binding.imgCharacter.setImageResource(fallbackId)
                binding.imgCharacter.visibility = View.VISIBLE
            } else {
                binding.imgCharacter.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateBackground() {
        val bgName = dialogueManager.getCurrentBackground()
        val resId = resources.getIdentifier(bgName, "drawable", packageName)
        if (resId != 0) binding.imgBackground.setImageResource(resId)
    }

    private fun updateHUD() {
        binding.tvBalance.text = "RM ${"%,d".format(gameState.balance)}"
        binding.tvMonth.text = "Month ${gameState.currentMonth}"
        val location = dialogueManager.getCurrentScene()?.location ?: ""
        binding.tvLocation.text = if (location.isNotEmpty()) "— $location —" else "— —"
    }

    private fun getSpeakerColor(character: String): Int {
        return when (character) {
            "clara"  -> Color.parseColor("#1D9E75")
            "aiden"  -> Color.parseColor("#D85A30")
            "evelyn" -> Color.parseColor("#7F77DD")
            "mc"     -> Color.parseColor("#EF9F27")
            else     -> Color.WHITE
        }
    }

    private fun handleSceneEnd() {
        val nextScene = dialogueManager.getCurrentScene()?.next ?: ""
        if (nextScene == "budget_screen" || nextScene.isEmpty()) {
            val intent = Intent(this, BudgetActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        } else {
            dialogueManager.goToScene(nextScene)
            displayCurrentLine()
        }
    }

    private fun loadGameState(): GameState {
        val json = getSharedPreferences("brokewoke", MODE_PRIVATE)
            .getString("gamestate", null)
        return if (json != null) Gson().fromJson(json, GameState::class.java)
        else GameState()
    }

    private fun saveGameState(gameState: GameState) {
        val json = Gson().toJson(gameState)
        getSharedPreferences("brokewoke", MODE_PRIVATE)
            .edit().putString("gamestate", json).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}