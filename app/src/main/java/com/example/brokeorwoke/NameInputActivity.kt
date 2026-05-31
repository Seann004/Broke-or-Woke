package com.example.brokeorwoke

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import com.example.brokeorwoke.databinding.ActivityNameInputBinding
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson

class NameInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameInputBinding
    private val handler = Handler(Looper.getMainLooper())

    private val claraLine = "Hey! Before your first day starts — what should I call you?"
    private var charIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        startAnimationSequence()
        setupInputHandlers()
    }

    private fun startAnimationSequence() {
        handler.postDelayed({ fadeUp(binding.tvChapter, 500) }, 300)
        handler.postDelayed({ fadeUp(binding.tvTitle, 500) }, 600)
        handler.postDelayed({ fadeUp(binding.divider, 400) }, 900)
        handler.postDelayed({ slideInCharacter() }, 700)
        handler.postDelayed({ fadeUp(binding.dialogueBubble, 400) }, 1200)
        handler.postDelayed({ startTypewriter() }, 1500)
        handler.postDelayed({
            fadeUp(binding.tvInputLabel, 400)
            fadeUp(binding.nameInputLayout, 400)
        }, 3000)
        handler.postDelayed({ fadeUp(binding.btnBegin, 500) }, 3400)
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

    private fun fadeUp(view: View, duration: Long) {
        view.translationY = 16f
        val fade = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply { this.duration = duration }
        val slide = ObjectAnimator.ofFloat(view, "translationY", 16f, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
        AnimatorSet().apply { playTogether(fade, slide); start() }
    }

    private fun setupInputHandlers() {
        binding.etPlayerName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptBegin()
                true
            } else false
        }
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

        val gameState = GameState(playerName = name)
        saveGameState(gameState)

        showPreGameQuiz(name)
    }

    private fun showPreGameQuiz(playerName: String) {
        val questions = listOf(
            Triple(
                "What percentage of your salary should you ideally save each month?",
                listOf("5%", "10%", "20%", "As much as possible"),
                2
            ),
            Triple(
                "What is an emergency fund?",
                listOf(
                    "Money saved for holidays",
                    "3-6 months of expenses set aside for unexpected events",
                    "A bank loan for emergencies",
                    "Money kept in your wallet"
                ),
                1
            ),
            Triple(
                "Which of these is considered a 'want' rather than a 'need'?",
                listOf("Rent", "Food", "Netflix subscription", "Transport to work"),
                2
            )
        )

        var correctAnswers = 0
        val playerAnswers = mutableListOf<Int>()
        var currentIndex = 0
        var selectedIndex = -1
        var answered = false

        val options = listOf(
            binding.optionA, binding.optionB, binding.optionC, binding.optionD
        )
        val optionTexts = listOf(
            binding.tvOptA, binding.tvOptB, binding.tvOptC, binding.tvOptD
        )
        val optionLabels = listOf(
            binding.tvOptALabel, binding.tvOptBLabel,
            binding.tvOptCLabel, binding.tvOptDLabel
        )

        fun resetOptions() {
            options.forEach { it.setBackgroundResource(R.drawable.dialogue_bubble_bg) }
            optionLabels.forEach { it.setTextColor(android.graphics.Color.parseColor("#EF9F27")) }
            optionTexts.forEach { it.setTextColor(android.graphics.Color.WHITE) }
        }

        fun loadQuestion(index: Int) {
            if (index >= questions.size) {
                val score = correctAnswers
                val msg = when {
                    score == 3 -> "3/3 — Impressive! You already know your basics. Let's see if you apply them."
                    score == 2 -> "2/3 — Not bad! You have some financial knowledge. Time to test it in real life."
                    score == 1 -> "1/3 — Some things to learn! That's exactly why we're here."
                    else ->       "0/3 — That's okay. Nobody teaches us this. You're about to learn it firsthand."
                }

                binding.tvQuestionNum.text = "Result: $score/3"
                binding.tvQuestionText.text = msg
                options.forEach { it.visibility = View.GONE }
                binding.tvQuizFeedback.visibility = View.GONE
                binding.btnQuizNext.text = "Let's go, $playerName! ▶"
                binding.btnQuizNext.setOnClickListener {
                    val gs = loadGameState()
                    playerAnswers.forEachIndexed { i, ans ->
                        val correct = ans == questions[i].third
                        gs.flags.add("pre_quiz_q${i + 1}_${if (correct) "correct" else "wrong"}")
                    }
                    saveGameState(gs)
                    binding.quizOverlay.visibility = View.GONE
                    startActivity(Intent(this, DialogueActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                return
            }

            val (question, opts, correctIdx) = questions[index]
            selectedIndex = -1
            answered = false

            binding.tvQuestionNum.text = "Question ${index + 1} of ${questions.size}"
            binding.tvQuestionText.text = question
            binding.tvQuizFeedback.visibility = View.GONE
            binding.btnQuizNext.text = "Confirm answer"
            binding.tvQuizLabel.text = "FINANCIAL KNOWLEDGE CHECK"

            resetOptions()
            options.forEach { it.visibility = View.VISIBLE }
            optionTexts.forEachIndexed { i, tv -> tv.text = opts[i] }

            options.forEachIndexed { i, opt ->
                opt.setOnClickListener {
                    if (answered) return@setOnClickListener
                    selectedIndex = i
                    resetOptions()
                    opt.setBackgroundColor(android.graphics.Color.parseColor("#33EF9F27"))
                    optionLabels[i].setTextColor(android.graphics.Color.WHITE)
                }
            }

            binding.btnQuizNext.setOnClickListener {
                if (answered) {
                    playerAnswers.add(selectedIndex)
                    currentIndex++
                    loadQuestion(currentIndex)
                    return@setOnClickListener
                }

                if (selectedIndex == -1) {
                    android.widget.Toast.makeText(
                        this, "Please select an answer first", android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                answered = true
                val isCorrect = selectedIndex == correctIdx

                if (isCorrect) {
                    correctAnswers++
                    options[selectedIndex].setBackgroundColor(android.graphics.Color.parseColor("#331D9E75"))
                    optionLabels[selectedIndex].setTextColor(android.graphics.Color.parseColor("#1D9E75"))
                    binding.tvQuizFeedback.setTextColor(android.graphics.Color.parseColor("#9FE1CB"))
                    binding.tvQuizFeedback.text = "✓ Correct! " + when (index) {
                        0 -> "The '20' in 50/30/20 — save at least 20% of your income every month."
                        1 -> "An emergency fund is 3-6 months of living expenses for unexpected events like medical bills or job loss."
                        2 -> "Netflix is a want. Rent, food and transport are needs."
                        else -> ""
                    }
                } else {
                    options[selectedIndex].setBackgroundColor(android.graphics.Color.parseColor("#33F09595"))
                    optionLabels[selectedIndex].setTextColor(android.graphics.Color.parseColor("#F09595"))
                    options[correctIdx].setBackgroundColor(android.graphics.Color.parseColor("#331D9E75"))
                    optionLabels[correctIdx].setTextColor(android.graphics.Color.parseColor("#1D9E75"))
                    binding.tvQuizFeedback.setTextColor(android.graphics.Color.parseColor("#CCFFFFFF"))
                    binding.tvQuizFeedback.text = "✗ Not quite. " + when (index) {
                        0 -> "The recommended rate is 20% — the '20' in the 50/30/20 rule."
                        1 -> "An emergency fund is 3-6 months of expenses for unexpected events — not a loan or holiday money."
                        2 -> "Netflix is a want. The correct answer is highlighted in green above."
                        else -> ""
                    }
                }

                binding.tvQuizFeedback.visibility = View.VISIBLE
                binding.btnQuizNext.text =
                    if (index < questions.size - 1) "Next question ▶" else "See result ▶"
            }
        }

        // Show intro screen first
        binding.tvQuizLabel.text = "BEFORE WE BEGIN"
        binding.tvQuestionNum.text = "Financial Knowledge Check"
        binding.tvQuestionText.text = "Hi $playerName! 3 quick questions to check your financial knowledge baseline. We will compare your answers at the end to see how much you have learned."
        options.forEach { it.visibility = View.GONE }
        binding.tvQuizFeedback.visibility = View.GONE
        binding.btnQuizNext.text = "Start Quiz ▶"
        binding.btnQuizNext.setOnClickListener {
            loadQuestion(0)
        }

        // Animate overlay in
        binding.quizOverlay.visibility = View.VISIBLE
        binding.quizCard.alpha = 0f
        binding.quizCard.scaleX = 0.9f
        binding.quizCard.scaleY = 0.9f
        ObjectAnimator.ofFloat(binding.quizCard, "alpha", 0f, 1f).apply { duration = 300; start() }
        ObjectAnimator.ofFloat(binding.quizCard, "scaleX", 0.9f, 1f).apply { duration = 300; start() }
        ObjectAnimator.ofFloat(binding.quizCard, "scaleY", 0.9f, 1f).apply { duration = 300; start() }
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