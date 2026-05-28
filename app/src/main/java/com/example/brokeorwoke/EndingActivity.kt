package com.example.brokeorwoke

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.brokeorwoke.databinding.ActivityEndingBinding
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson

class EndingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEndingBinding
    private lateinit var gameState: GameState
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEndingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set fullscreen immersive mode
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        gameState = loadGameState()

        val score = calculateScore()
        val ending = determineEnding()

        setupBackground()
        setupEndingText(ending, score)
        setupStats(score)
        setupRelationships()
        setupEndingDialogue(ending)
        setupTip(score)
        startAnimationSequence()

        binding.btnPlayAgain.setOnClickListener {
            clearGameState()
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    // Calculate the player's score based on game state
    private fun calculateScore(): Int {
        var score = 50

        val savingsPercent = if (gameState.balance > 0)
            (gameState.savings.toFloat() / 2500 * 100).toInt()
        else 0

        score += when {
            savingsPercent >= 20 -> 30
            savingsPercent >= 10 -> 15
            savingsPercent > 0   -> 5
            else                 -> 0
        }

        score -= when {
            gameState.debt >= 1000 -> 30
            gameState.debt >= 500  -> 20
            gameState.debt >= 200  -> 10
            gameState.debt > 0     -> 5
            else                   -> 0
        }

        val goodFlags = listOf(
            "responsible_car", "used_emergency_fund", "resisted_shopee",
            "bonus_saved", "bonus_split", "adjusted_budget",
            "negotiated_rent", "planner", "pay_yourself_first",
            "emergency_fund_priority", "repaid_aiden_full", "validated_aiden"
        )
        score += gameState.flags.count { it in goodFlags } * 3

        val badFlags = listOf(
            "ignored_car", "medical_debt", "shopee_splurge",
            "bonus_spent", "ignored_rent", "zero_savings_m1",
            "zero_savings_m2", "zero_savings_m3", "avoided_aiden_debt"
        )
        score -= gameState.flags.count { it in badFlags } * 4

        return score.coerceIn(0, 100)
    }

    // Determine the ending based on relationship scores
    private fun determineEnding(): String {
        val clara = gameState.claraRel
        val aiden = gameState.aidenRel
        val evelyn = gameState.evelynRel

        return when {
            clara >= aiden && clara >= evelyn && clara >= 20 -> "clara"
            aiden >= clara && aiden >= evelyn && aiden >= 20 -> "aiden"
            evelyn >= clara && evelyn >= aiden && evelyn >= 20 -> "evelyn"
            else -> "neutral"
        }
    }

    // Set the background image for the ending screen
    private fun setupBackground() {
        val resId = resources.getIdentifier("bg_spring_outdoor", "drawable", packageName)
        if (resId != 0) binding.imgBackground.setImageResource(resId)
    }

    // Set the ending text and title based on the score and ending
    private fun setupEndingText(ending: String, score: Int) {
        val (type, title) = when {
            score >= 70 && ending == "clara" ->
                "FINANCIALLY WOKE" to "The Right Path"
            score >= 70 && ending == "evelyn" ->
                "STILL LEARNING" to "Growing Together"
            score >= 70 ->
                "FINANCIALLY WOKE" to "You Made It"
            score >= 50 ->
                "GETTING THERE" to "One Step at a Time"
            ending == "aiden" ->
                "BROKE BUT ALIVE" to "The Hard Lesson"
            else ->
                "BROKE" to "Back to Zero"
        }

        binding.tvEndingType.text = type
        binding.tvEndingTitle.text = title
        binding.tvEndingTitle.setTextColor(
            when {
                score >= 70 -> Color.parseColor("#5DCAA5")
                score >= 50 -> Color.parseColor("#EF9F27")
                else        -> Color.parseColor("#F09595")
            }
        )
    }

    // Display the player's stats on the ending screen
    private fun setupStats(score: Int) {
        binding.tvStatBalance.text = "RM ${"%,d".format(gameState.balance)}"
        binding.tvStatSavings.text = "RM ${"%,d".format(gameState.savings)}"
        binding.tvScore.text = "$score/100"

        if (gameState.debt == 0) {
            binding.tvStatDebt.setTextColor(Color.parseColor("#1D9E75"))
            binding.tvStatDebt.text = "None ✓"
        } else {
            binding.tvStatDebt.text = "RM ${"%,d".format(gameState.debt)}"
        }

        handler.postDelayed({
            binding.scoreProgress.progress = score
        }, 1000)
    }

    // Display the relationship progress bars
    private fun setupRelationships() {
        handler.postDelayed({
            binding.claraProgress.progress = gameState.claraRel
            binding.tvClaraRel.text = "${gameState.claraRel}"
            binding.aidenProgress.progress = gameState.aidenRel
            binding.tvAidenRel.text = "${gameState.aidenRel}"
            binding.evelynProgress.progress = gameState.evelynRel
            binding.tvEvelynRel.text = "${gameState.evelynRel}"
        }, 1200)
    }

    // Display the ending dialogue based on the ending
    private fun setupEndingDialogue(ending: String) {
        val score = calculateScore()

        val speaker: String
        val character: String
        val colour: String
        val line: String

        when (ending) {
            "clara" -> {
                speaker = "Ms. Clara"
                character = "clara"
                colour = "#1D9E75"
                line = if (score >= 70)
                    "You figured it out. Most people don't — not this fast. I'm proud of you."
                else
                    "It wasn't perfect. But you tried. That counts for something. Keep going."
            }
            "aiden" -> {
                speaker = "Aiden"
                character = "aiden"
                colour = "#D85A30"
                line = if (score < 50)
                    "Okay bro... I think we both learned something this month. Maybe Clara had a point."
                else
                    "See? You survived AND had fun. Balance is everything. Well... mostly fun."
            }
            "evelyn" -> {
                speaker = "Evelyn"
                character = "evelyn"
                colour = "#7F77DD"
                line = "We're both still figuring it out. But at least we're figuring it out together, right?"
            }
            else -> {
                speaker = "Ms. Clara"
                character = "clara"
                colour = "#1D9E75"
                line = "Three months done. Whatever happened — you learned something. Use it."
            }
        }

        binding.tvEndingSpeaker.text = speaker
        binding.tvEndingSpeaker.setTextColor(Color.parseColor(colour))
        binding.tvEndingDialogue.text = line

        // Show character sprite
        val drawableName = "char_${character}_neutral"
        val resId = resources.getIdentifier(drawableName, "drawable", packageName)
        if (resId != 0) {
            binding.imgCharacter.setImageResource(resId)
        }
    }

    // Display a financial tip based on the player's performance
    private fun setupTip(score: Int) {
        binding.tvTip.text = when {
            gameState.debt > 500 ->
                "Debt snowballs fast. Always pay off debt before spending on wants. Even RM 50 extra per month makes a difference."
            gameState.savings < 200 ->
                "The 50/30/20 rule: 50% needs, 30% wants, 20% savings. Even if you can't hit 20%, start with 10%."
            score >= 70 ->
                "You saved well! Next step: build 3 months of expenses as an emergency fund. That's your safety net."
            gameState.flags.contains("resisted_shopee") ->
                "Resisting impulse buys is one of the hardest financial skills. You proved you can do it."
            gameState.flags.contains("pay_yourself_first") ->
                "Paying yourself first is how wealth is built — not what's left over, but what you set aside first."
            else ->
                "Every month is a fresh start. Review what worked, cut what didn't, and keep the savings habit going."
        }
    }

    // Start the animation sequence for the ending screen
    private fun startAnimationSequence() {
        handler.postDelayed({ fadeUp(binding.tvEndingType, 500) }, 300)
        handler.postDelayed({ fadeUp(binding.tvEndingTitle, 500) }, 450)
        handler.postDelayed({ fadeUp(binding.divider, 400) }, 600)
        handler.postDelayed({ fadeUp(binding.statsCard, 500) }, 750)

        handler.postDelayed({
            binding.imgCharacter.alpha = 0f
            binding.imgCharacter.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(binding.imgCharacter, "alpha", 0f, 1f).apply {
                duration = 800
                start()
            }
        }, 500)

        handler.postDelayed({ fadeUp(binding.relCard, 500) }, 900)
        handler.postDelayed({ fadeUp(binding.endingDialogue, 500) }, 1100)
        handler.postDelayed({ fadeUp(binding.btnPlayAgain, 500) }, 1300)
    }

    // Fade-in animation for a view
    private fun fadeUp(view: View, duration: Long) {
        view.translationY = 20f
        val fade = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            this.duration = duration
        }
        val slide = ObjectAnimator.ofFloat(view, "translationY", 20f, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
        AnimatorSet().apply {
            playTogether(fade, slide)
            start()
        }
    }

    // Clear the saved game state
    private fun clearGameState() {
        getSharedPreferences("brokewoke", MODE_PRIVATE)
            .edit().clear().apply()
    }

    // Load the saved game state
    private fun loadGameState(): GameState {
        val json = getSharedPreferences("brokewoke", MODE_PRIVATE)
            .getString("gamestate", null)
        return if (json != null) Gson().fromJson(json, GameState::class.java)
        else GameState()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}