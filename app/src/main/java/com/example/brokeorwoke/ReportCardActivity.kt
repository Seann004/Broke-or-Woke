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
import com.example.brokeorwoke.databinding.ActivityReportCardBinding
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson

class ReportCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportCardBinding
    private lateinit var gameState: GameState
    private val handler = Handler(Looper.getMainLooper())

    // These are passed from BudgetActivity via Intent extras
    private var rentAmount = 0
    private var foodAmount = 0
    private var transportAmount = 0
    private var wantsAmount = 0
    private var savingsAmount = 0
    private var salary = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        gameState = loadGameState()

        // Receive budget amounts from BudgetActivity
        rentAmount = intent.getIntExtra("rent", 0)
        foodAmount = intent.getIntExtra("food", 0)
        transportAmount = intent.getIntExtra("transport", 0)
        wantsAmount = intent.getIntExtra("wants", 0)
        savingsAmount = intent.getIntExtra("savings", 0)
        salary = intent.getIntExtra("salary", 2500)

        setupHeader()
        setupMetrics()
        setupAchievement()
        setupClaraFeedback()
        setupFinancialFact()
        startAnimations()

        binding.btnContinue.setOnClickListener {
            navigateToEvent()
        }
    }

    private fun setupHeader() {
        binding.tvMonthLabel.text = "MONTH ${gameState.currentMonth} FINANCIAL REPORT"
        binding.btnContinue.text = if (gameState.currentMonth < 3)
            "Continue to Month ${gameState.currentMonth + 1} ▶"
        else
            "Final Month Done ▶"
    }

    private fun setupMetrics() {
        if (salary <= 0) return

        // Calculate percentages
        val savingsPercent = (savingsAmount.toFloat() / salary * 100).toInt()
        val needsAmount = rentAmount + foodAmount + transportAmount
        val needsPercent = (needsAmount.toFloat() / salary * 100).toInt()
        val wantsPercent = (wantsAmount.toFloat() / salary * 100).toInt()

        // Savings rate
        binding.tvSavingsRate.text = "$savingsPercent%"
        if (savingsPercent >= 20) {
            binding.tvSavingsRate.setTextColor(Color.parseColor("#1D9E75"))
            binding.tvSavingsIcon.text = " ✓"
            binding.tvSavingsIcon.setTextColor(Color.parseColor("#1D9E75"))
        } else if (savingsPercent >= 10) {
            binding.tvSavingsRate.setTextColor(Color.parseColor("#EF9F27"))
            binding.tvSavingsIcon.text = " ⚠"
            binding.tvSavingsIcon.setTextColor(Color.parseColor("#EF9F27"))
        } else {
            binding.tvSavingsRate.setTextColor(Color.parseColor("#F09595"))
            binding.tvSavingsIcon.text = " ✗"
            binding.tvSavingsIcon.setTextColor(Color.parseColor("#F09595"))
        }

        // Needs rate
        binding.tvNeedsRate.text = "$needsPercent%"
        if (needsPercent <= 50) {
            binding.tvNeedsRate.setTextColor(Color.parseColor("#1D9E75"))
            binding.tvNeedsIcon.text = " ✓"
            binding.tvNeedsIcon.setTextColor(Color.parseColor("#1D9E75"))
        } else if (needsPercent <= 60) {
            binding.tvNeedsRate.setTextColor(Color.parseColor("#EF9F27"))
            binding.tvNeedsIcon.text = " ⚠"
            binding.tvNeedsIcon.setTextColor(Color.parseColor("#EF9F27"))
        } else {
            binding.tvNeedsRate.setTextColor(Color.parseColor("#F09595"))
            binding.tvNeedsIcon.text = " ✗"
            binding.tvNeedsIcon.setTextColor(Color.parseColor("#F09595"))
        }

        // Wants rate
        binding.tvWantsRate.text = "$wantsPercent%"
        if (wantsPercent <= 30) {
            binding.tvWantsRate.setTextColor(Color.parseColor("#1D9E75"))
            binding.tvWantsIcon.text = " ✓"
            binding.tvWantsIcon.setTextColor(Color.parseColor("#1D9E75"))
        } else {
            binding.tvWantsRate.setTextColor(Color.parseColor("#EF9F27"))
            binding.tvWantsIcon.text = " ⚠"
            binding.tvWantsIcon.setTextColor(Color.parseColor("#EF9F27"))
        }

        // Total savings
        binding.tvSavingsTotal.text = "RM ${"%,d".format(gameState.savings)}"
        if (gameState.savings >= 500) {
            binding.tvSavingsTotal.setTextColor(Color.parseColor("#1D9E75"))
            binding.tvSavingsIcon2.text = " ✓"
            binding.tvSavingsIcon2.setTextColor(Color.parseColor("#1D9E75"))
        } else if (gameState.savings > 0) {
            binding.tvSavingsTotal.setTextColor(Color.parseColor("#EF9F27"))
            binding.tvSavingsIcon2.text = " ⚠"
            binding.tvSavingsIcon2.setTextColor(Color.parseColor("#EF9F27"))
        } else {
            binding.tvSavingsTotal.setTextColor(Color.parseColor("#F09595"))
            binding.tvSavingsIcon2.text = " ✗"
            binding.tvSavingsIcon2.setTextColor(Color.parseColor("#F09595"))
        }
    }

    private fun setupAchievement() {
        val savingsPercent = if (salary > 0) (savingsAmount.toFloat() / salary * 100).toInt() else 0

        // Check milestones
        val (title, desc) = when {
            gameState.savings >= 1000 && !gameState.flags.contains("milestone_1000") -> {
                gameState.flags.add("milestone_1000")
                saveGameState(gameState)
                "RM 1,000 Saved!" to "You now have RM 1,000 in savings. Financial experts recommend building up to 3 months of expenses (about RM 4,500). You are almost a quarter of the way there!"
            }
            gameState.savings >= 500 && !gameState.flags.contains("milestone_500") -> {
                gameState.flags.add("milestone_500")
                saveGameState(gameState)
                "Emergency Fund Started!" to "RM 500 saved! This is your first safety net. A full emergency fund covers 3 months of expenses. Keep going — every ringgit counts."
            }
            savingsPercent >= 20 && !gameState.flags.contains("milestone_20pct") -> {
                gameState.flags.add("milestone_20pct")
                saveGameState(gameState)
                "20% Savings Rate!" to "You hit the recommended 20% savings rate this month. The 50/30/20 rule in action. If you keep this up every month, you will have over RM 1,500 saved by Month 3."
            }
            gameState.debt == 0 && gameState.currentMonth > 1 && !gameState.flags.contains("milestone_debt_free") -> {
                gameState.flags.add("milestone_debt_free")
                saveGameState(gameState)
                "Debt Free!" to "You have reached Month ${gameState.currentMonth} with zero debt. Most fresh graduates carry debt by their second month. You are already ahead."
            }
            else -> null to null
        }

        if (title != null && desc != null) {
            binding.achievementBanner.visibility = View.VISIBLE
            binding.tvAchievementTitle.text = title
            binding.tvAchievementDesc.text = desc
        } else {
            binding.achievementBanner.visibility = View.GONE
        }
    }

    private fun setupClaraFeedback() {
        val savingsPercent = if (salary > 0) (savingsAmount.toFloat() / salary * 100).toInt() else 0
        val needsPercent = if (salary > 0) ((rentAmount + foodAmount + transportAmount).toFloat() / salary * 100).toInt() else 0
        val wantsPercent = if (salary > 0) (wantsAmount.toFloat() / salary * 100).toInt() else 0

        binding.tvClaraFeedback.text = when {
            savingsPercent >= 20 && needsPercent <= 50 ->
                "This is exactly what the 50/30/20 rule looks like in practice. Your savings rate is on target and your needs are under control. This is how wealth is built — not in big moments, but in consistent months like this one."
            savingsPercent >= 20 && needsPercent > 50 ->
                "Your savings rate is good — that is the most important number. But your needs are running a bit high at $needsPercent%. Check if your rent or transport can be reduced next month. Small cuts add up."
            savingsPercent >= 10 ->
                "Saving $savingsPercent% is a start, but the target is 20%. You are ${20 - savingsPercent}% short. Look at your wants — RM $wantsAmount this month. That is where the gap usually is."
            savingsPercent > 0 ->
                "You saved something, which is better than nothing. But at $savingsPercent%, you would not have enough to cover a RM 500 emergency without going into debt. Next month, try to get savings above 15%."
            gameState.debt > 0 ->
                "You have RM ${gameState.debt} in debt right now. Before anything else, next month's priority is clearing that. Debt costs you money every month you carry it. Deal with it first."
            else ->
                "No savings this month. I am not going to lecture you — but I want you to think about what happens if your car breaks down next month and you have nothing set aside. That is exactly what happened to me at 23."
        }
    }

    private fun setupFinancialFact() {
        val savingsPercent = if (salary > 0) (savingsAmount.toFloat() / salary * 100).toInt() else 0

        binding.tvFinancialFact.text = when {
            gameState.currentMonth == 1 && savingsPercent >= 20 ->
                "Did you know? If you save 20% of RM 2,500 every month for a year, you will have RM 6,000 saved — enough to cover more than 2 months of expenses in an emergency."
            gameState.currentMonth == 1 ->
                "The 50/30/20 rule: allocate 50% of your income to needs, 30% to wants, and 20% to savings. It is the simplest budgeting framework for fresh graduates."
            gameState.currentMonth == 2 && gameState.savings >= 500 ->
                "An emergency fund should cover 3 to 6 months of expenses. For a RM 2,500 salary, that means RM 4,500 to RM 9,000. You have RM ${gameState.savings} so far — keep going."
            gameState.currentMonth == 2 ->
                "Compound interest works both ways — it grows your savings and it grows your debt. A RM 500 debt at 18% annual interest becomes RM 590 in a year without a single new purchase."
            gameState.currentMonth == 3 ->
                "Paying yourself first means transferring your savings the moment your salary arrives — before spending on anything else. This single habit separates those who build wealth from those who do not."
            else ->
                "Small consistent savings beat irregular large ones. RM 500 saved every month for 10 years, invested at 6% annual return, grows to over RM 81,000."
        }
    }



    private fun startAnimations() {
        // Month label
        handler.postDelayed({ fadeUp(binding.tvMonthLabel, 400) }, 200)



        // Achievement banner if visible
        if (binding.achievementBanner.visibility == View.VISIBLE) {
            binding.achievementBanner.alpha = 0f
            handler.postDelayed({ fadeUp(binding.achievementBanner, 500) }, 500)
        }

        // Report card
        handler.postDelayed({ fadeUp(binding.reportCard, 500) }, 700)

        // Clara feedback
        handler.postDelayed({ fadeUp(binding.claraFeedback, 500) }, 1000)

        // Financial fact
        handler.postDelayed({ fadeUp(binding.factBox, 500) }, 1200)

        // Continue button
        handler.postDelayed({ fadeUp(binding.btnContinue, 500) }, 1400)
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

    private fun navigateToEvent() {
        val intent = Intent(this, EventActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun loadGameState(): GameState {
        val json = getSharedPreferences("brokewoke", MODE_PRIVATE).getString("gamestate", null)
        return if (json != null) Gson().fromJson(json, GameState::class.java) else GameState()
    }

    private fun saveGameState(gameState: GameState) {
        val json = Gson().toJson(gameState)
        getSharedPreferences("brokewoke", MODE_PRIVATE).edit().putString("gamestate", json).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}