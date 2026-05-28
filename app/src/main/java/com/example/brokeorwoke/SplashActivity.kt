package com.example.brokeorwoke

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.brokeorwoke.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    // Typewriter lines
    private val typewriterLines = listOf(
        "Your first salary just dropped.",
        "RM 2,500. One month to survive.",
        "Everyone wants a piece of it.",
        "The question is — what will you do?"
    )
    private var currentLine = 0
    private var currentChar = 0
    private var isTyping = false

    // Coin symbols
    private val coinSymbols = listOf("💰", "💸", "🪙", "💵", "RM")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide status bar for full immersion
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        startAnimationSequence()
        startCoinSpawner()

    binding.btnBegin.setOnClickListener {
            startActivity(Intent(this, NameInputActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun startAnimationSequence() {

        // 1. Eyebrow fades up — 300ms delay
        handler.postDelayed({
            fadeUp(binding.tvEyebrow, 600)
        }, 300)

        // 2. BROKE punches in — 600ms delay
        handler.postDelayed({
            punchIn(binding.tvBroke, 500)
        }, 600)

        // 3. OR fades up — 1000ms delay
        handler.postDelayed({
            fadeUp(binding.tvOr, 400)
        }, 1000)

        // 4. WOKE? punches in — 1200ms delay
        handler.postDelayed({
            punchIn(binding.tvWoke, 500)
        }, 1200)

        // 5. Accent line expands — 1600ms delay
        handler.postDelayed({
            expandLine(binding.accentLine)
        }, 1600)

        // 6. Typewriter text appears — 1800ms delay
        handler.postDelayed({
            fadeUp(binding.tvTypewriter, 400)
            isTyping = true
            startTypewriter()
        }, 1800)

        // 7. Stats row fades up — 2800ms delay
        handler.postDelayed({
            fadeUp(binding.statsRow, 500)
        }, 2800)

        // 8. Begin button fades up — 3200ms delay
        handler.postDelayed({
            fadeUp(binding.btnBegin, 500)
            fadeUp(binding.tvCredit, 500)
        }, 3200)
    }

    // Fade + slide up animation
    private fun fadeUp(view: android.view.View, duration: Long) {
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

    // Punch/scale-in animation for BROKE and WOKE
    private fun punchIn(view: android.view.View, duration: Long) {
        view.scaleX = 0.6f
        view.scaleY = 0.6f
        val fade = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            this.duration = duration
        }
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.6f, 1f).apply {
            this.duration = duration
            interpolator = OvershootInterpolator(1.5f)
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.6f, 1f).apply {
            this.duration = duration
            interpolator = OvershootInterpolator(1.5f)
        }
        AnimatorSet().apply {
            playTogether(fade, scaleX, scaleY)
            start()
        }
    }

    // Expand the teal accent line from 0 to 48dp
    private fun expandLine(view: android.view.View) {
        val targetWidth = (48 * resources.displayMetrics.density).toInt()
        val anim = ObjectAnimator.ofInt(targetWidth).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                view.layoutParams = view.layoutParams.also { lp -> lp.width = value }
                view.requestLayout()
            }
        }
        anim.start()
    }

    // Typewriter effect cycling through lines
    private fun startTypewriter() {
        if (!isTyping) return
        val line = typewriterLines[currentLine]
        if (currentChar < line.length) {
            binding.tvTypewriter.text = line.substring(0, currentChar + 1) + "|"
            currentChar++
            handler.postDelayed({ startTypewriter() }, 45)
        } else {
            // Finished line — wait then clear and move to next
            binding.tvTypewriter.text = line
            handler.postDelayed({
                currentChar = 0
                currentLine = (currentLine + 1) % typewriterLines.size
                binding.tvTypewriter.text = ""
                handler.postDelayed({ startTypewriter() }, 300)
            }, 1800)
        }
    }

    // Spawn falling coin TextViews
    private fun startCoinSpawner() {
        val spawnRunnable = object : Runnable {
            override fun run() {
                spawnCoin()
                handler.postDelayed(this, 700)
            }
        }
        handler.post(spawnRunnable)
    }

    private fun spawnCoin() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        val coin = TextView(this).apply {
            text = coinSymbols.random()
            textSize = (10 + (Math.random() * 10)).toFloat()
            setTextColor(Color.WHITE)
            alpha = 0.5f
            gravity = Gravity.CENTER
        }

        val startX = (Math.random() * screenWidth).toFloat()
        coin.x = startX
        coin.y = -40f

        binding.coinContainer.addView(coin)

        val fallDuration = (2500 + Math.random() * 3000).toLong()

        ObjectAnimator.ofFloat(coin, "y", -40f, screenHeight.toFloat() + 40f).apply {
            duration = fallDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                // slight horizontal drift
                coin.x = startX + (Math.sin(it.animatedFraction * Math.PI * 2) * 20).toFloat()
            }
            start()
        }

        ObjectAnimator.ofFloat(coin, "alpha", 0.5f, 0f).apply {
            duration = fallDuration
            startDelay = fallDuration / 2
            start()
        }

        // Remove from layout after animation
        handler.postDelayed({
            binding.coinContainer.removeView(coin)
        }, fallDuration + 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        isTyping = false
        handler.removeCallbacksAndMessages(null)
    }
}