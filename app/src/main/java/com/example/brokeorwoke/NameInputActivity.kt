package com.example.brokeorwoke

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brokeorwoke.databinding.ActivityNameInputBinding
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson

class NameInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameInputBinding
    private val handler = Handler(Looper.getMainLooper())

    // Typewriter for Clara's dialogue
    private val claraLine = "Hey! Before your first day starts — what should I call you?"
    private var charIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Full screen immersion
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        startAnimationSequence()
        setupInputHandlers()
    }

    private fun startAnimationSequence() {

        // 1. Chapter label fades in
        handler.postDelayed({
            fadeUp(binding.tvChapter, 500)
        }, 300)

        // 2. Title fades in
        handler.postDelayed({
            fadeUp(binding.tvTitle, 500)
        }, 600)

        // 3. Divider fades in
        handler.postDelayed({
            fadeUp(binding.divider, 400)
        }, 900)

        // 4. Clara slides in from left
        handler.postDelayed({
            slideInCharacter()
        }, 700)

        // 5. Dialogue bubble appears
        handler.postDelayed({
            fadeUp(binding.dialogueBubble, 400)
        }, 1200)

        // 6. Typewriter starts
        handler.postDelayed({
            startTypewriter()
        }, 1500)

        // 7. Input field appears
        handler.postDelayed({
            fadeUp(binding.tvInputLabel, 400)
            fadeUp(binding.nameInputLayout, 400)
        }, 3000)

        // 8. Button appears
        handler.postDelayed({
            fadeUp(binding.btnBegin, 500)
        }, 3400)
    }

    private fun slideInCharacter() {
        binding.imgCharacter.apply {
            alpha = 0f
            translationX = -60f
        }
        val fade = ObjectAnimator.ofFloat(binding.imgCharacter, "alpha", 0f, 1f).apply {
            duration = 600
        }
        val slide = ObjectAnimator.ofFloat(binding.imgCharacter, "translationX", -60f, 0f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        AnimatorSet().apply {
            playTogether(fade, slide)
            start()
        }
    }

    private fun startTypewriter() {
        if (charIndex < claraLine.length) {
            binding.tvDialogue.text = claraLine.substring(0, charIndex + 1)
            charIndex++
            handler.postDelayed({ startTypewriter() }, 40)
        }
    }

    private fun fadeUp(view: android.view.View, duration: Long) {
        view.translationY = 16f
        val fade = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            this.duration = duration
        }
        val slide = ObjectAnimator.ofFloat(view, "translationY", 16f, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
        AnimatorSet().apply {
            playTogether(fade, slide)
            start()
        }
    }

    private fun setupInputHandlers() {

        // Done button on keyboard triggers same as begin button
        binding.etPlayerName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptBegin()
                true
            } else false
        }

        // Begin button
        binding.btnBegin.setOnClickListener {
            attemptBegin()
        }
    }

    private fun attemptBegin() {
        val name = binding.etPlayerName.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Please enter your name to begin"
            return
        }

        if (name.length < 2) {
            binding.nameInputLayout.error = "Name must be at least 2 characters"
            return
        }

        binding.nameInputLayout.error = null

        // Save to GameState via SharedPreferences
        val gameState = GameState(playerName = name)
        saveGameState(gameState)

        val intent = Intent(this, DialogueActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun saveGameState(gameState: GameState) {
        val json = Gson().toJson(gameState)
        getSharedPreferences("brokewoke", MODE_PRIVATE)
            .edit()
            .putString("gamestate", json)
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}