package com.example.brokeorwoke

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.brokeorwoke.databinding.ActivityBudgetBinding
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson

class BudgetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetBinding
    private lateinit var gameState: GameState

    private var salary = 2500
    private var rentAmount = 0
    private var foodAmount = 0
    private var transportAmount = 0
    private var wantsAmount = 0
    private var savingsAmount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        gameState = loadGameState()
        val monthlySalary = 2500
        salary = gameState.balance

        // Show breakdown
        binding.tvMonthLabel.text = "MONTH ${gameState.currentMonth} BUDGET"
        binding.tvSalaryAmount.text = "RM ${"%,d".format(monthlySalary)}"
        binding.tvSavingsCarried.text = "RM ${"%,d".format(gameState.savings)}"
        binding.tvSalary.text = "RM ${"%,d".format(salary)}"

        // Show debt row only if debt exists
        if (gameState.debt > 0) {
            binding.debtRow.visibility = View.VISIBLE
            binding.tvDebtAmount.text = "RM ${"%,d".format(gameState.debt)}"
        }

        updateRemaining()
        updateClaraExpression()
        setupSliders()

        binding.btnConfirm.setOnClickListener {
            attemptConfirm()
        }
    }

    private fun setupSliders() {
        binding.seekRent.max = salary
        binding.seekFood.max = salary
        binding.seekTransport.max = salary
        binding.seekWants.max = salary
        binding.seekSavings.max = salary

        val minRent = when {
            "min_rent_850" in gameState.flags -> 850
            "min_rent_800" in gameState.flags -> 800
            else -> 750
        }

        binding.seekRent.progress = minRent
        rentAmount = minRent
        binding.tvRentAmount.text = "RM $minRent"

        binding.seekRent.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Enforce minimum rent for API 24 compatibility
                if (progress < minRent && fromUser) {
                    seekBar.progress = minRent
                    return
                }
                rentAmount = maxOf(snapToStep(progress, 50), minRent)
                binding.tvRentAmount.text = "RM $rentAmount"
                updateRemaining()
                updateClaraExpression()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekFood.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                foodAmount = snapToStep(progress, 50)
                binding.tvFoodAmount.text = "RM $foodAmount"
                updateRemaining()
                updateClaraExpression()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekTransport.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                transportAmount = snapToStep(progress, 50)
                binding.tvTransportAmount.text = "RM $transportAmount"
                updateRemaining()
                updateClaraExpression()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekWants.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                wantsAmount = snapToStep(progress, 50)
                binding.tvWantsAmount.text = "RM $wantsAmount"
                updateRemaining()
                updateClaraExpression()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekSavings.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                savingsAmount = snapToStep(progress, 50)
                binding.tvSavingsAmount.text = "RM $savingsAmount"
                updateRemaining()
                updateClaraExpression()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    // Snap slider to nearest step (e.g. 50) for cleaner values
    private fun snapToStep(value: Int, step: Int): Int {
        return (value / step) * step
    }

    private fun updateRemaining() {
        val spent = rentAmount + foodAmount + transportAmount + wantsAmount + savingsAmount
        val remaining = salary - spent

        binding.tvRemaining.text = "RM $remaining"

        // Change remaining colour based on status
        when {
            remaining < 0 -> {
                binding.tvRemaining.setTextColor(android.graphics.Color.parseColor("#F09595"))
                showWarning("⚠ You've exceeded your salary! Reduce some categories.")
            }
            remaining == 0 -> {
                binding.tvRemaining.setTextColor(android.graphics.Color.parseColor("#1D9E75"))
                hideWarning()
            }
            remaining <= 200 -> {
                binding.tvRemaining.setTextColor(android.graphics.Color.parseColor("#EF9F27"))
                hideWarning()
            }
            else -> {
                binding.tvRemaining.setTextColor(android.graphics.Color.parseColor("#1D9E75"))
                hideWarning()
            }
        }

    }

    private fun updateClaraExpression() {
        val minRent = when {
            "min_rent_850" in gameState.flags -> 850
            "min_rent_800" in gameState.flags -> 800
            else -> 0
        }

        if (minRent > 0 && rentAmount <= minRent) {
            binding.tvClaraAdvice.text =
                "Your rent increased to RM $minRent this month. That's ${(minRent.toFloat()/salary*100).toInt()}% of your salary — you'll need to cut elsewhere."
            return
        }

        val savingsPercent = (savingsAmount.toFloat() / salary * 100).toInt()
        val spent = rentAmount + foodAmount + transportAmount + wantsAmount + savingsAmount
        val remaining = salary - spent

        val advice = when {
            remaining < 0 ->
                "You're overspending! Cut something — your future self will thank you."
            savingsPercent >= 20 ->
                "That's the 50/30/20 rule in action. You're doing great. 🎉"
            savingsPercent >= 10 ->
                "Good start — try to push savings to at least 20% if you can."
            savingsAmount > 0 ->
                "Saving something is better than nothing, but RM ${savingsAmount} might not cover an emergency."
            wantsAmount > 750 ->
                "Wants above RM 750 is risky. What happens if your car breaks down?"
            rentAmount > 1000 ->
                "Rent over RM 1,000 on RM 2,500 salary is tough. That's ${(rentAmount.toFloat()/salary*100).toInt()}% of your income."
            else ->
                "Try to save at least RM 500 this month. Even small amounts add up."
        }

        binding.tvClaraAdvice.text = advice
    }

    private fun showWarning(message: String) {
        binding.tvWarning.text = message
        binding.tvWarning.visibility = View.VISIBLE
    }

    private fun hideWarning() {
        binding.tvWarning.visibility = View.GONE
    }

    private fun attemptConfirm() {
        val spent = rentAmount + foodAmount + transportAmount + wantsAmount + savingsAmount
        val remaining = salary - spent

        if (remaining < 0) {
            showWarning("⚠ You've exceeded your salary! Reduce some categories.")
            return
        }

        if (savingsAmount == 0) {
            showWarning("⚠ Are you sure you want to save nothing this month?")
        }

        // Save budget decisions to GameState
        gameState.balance = remaining
        gameState.savings = gameState.savings + savingsAmount

        // Flag if they saved well or poorly
        val savingsPercent = (savingsAmount.toFloat() / salary * 100).toInt()
        when {
            savingsPercent >= 20 -> gameState.flags.add("good_saver_m${gameState.currentMonth}")
            savingsPercent == 0  -> gameState.flags.add("zero_savings_m${gameState.currentMonth}")
            savingsPercent < 10  -> gameState.flags.add("poor_saver_m${gameState.currentMonth}")
        }

        // Clara relationship bonus for good saving
        if (savingsPercent >= 20) {
            gameState.claraRel = (gameState.claraRel + 10).coerceAtMost(100)
        }

        saveGameState(gameState)

        // Route based on current month
        val intent = Intent(this, EventActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
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
}