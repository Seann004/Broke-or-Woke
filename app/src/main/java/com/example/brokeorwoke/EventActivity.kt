package com.example.brokeorwoke

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
import com.example.brokeorwoke.databinding.ActivityEventBinding
import com.example.brokeorwoke.model.GameState
import com.google.gson.Gson

// Data class for a single event
data class GameEvent(
    val id: String,
    val title: String,
    val description: String,
    val type: String,          // "expense", "income", "social"
    val background: String,
    val character: String,     // "clara", "aiden", "evelyn", ""
    val expression: String,
    val characterLine: String,
    val choices: List<EventChoice>
)

data class EventChoice(
    val text: String,
    val balanceEffect: Int,
    val savingsEffect: Int,
    val debtEffect: Int,
    val claraRel: Int,
    val aidenRel: Int,
    val evelynRel: Int,
    val flag: String,
    val resultText: String,
    val resultColor: String    // "good", "bad", "neutral"
)

class EventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventBinding
    private lateinit var gameState: GameState
    private lateinit var currentEvent: GameEvent
    private val handler = Handler(Looper.getMainLooper())

    // Pool of random events
    private val eventPool = listOf(

        GameEvent(
            id = "car_breakdown",
            title = "Car Breakdown! 🚗",
            description = "Your Myvi just broke down on the way to work. The mechanic says it needs RM 650 to fix. What do you do?",
            type = "expense",
            background = "bg_office_hallway",
            character = "aiden",
            expression = "worried",
            characterLine = "Bro... RM 650? That's half my monthly wants budget. You got emergency fund or not?",
            choices = listOf(
                EventChoice("Pay full from savings — fix it properly.", -650, -650, 0, 10, -5, 0, "responsible_car", "Smart move. Car's fixed, savings took a hit but you're covered.", "neutral"),
                EventChoice("Pay half now, borrow rest from Aiden.", -325, 0, 325, -5, 10, 0, "borrowed_aiden", "Temporary fix. But now you owe Aiden RM 325 next month.", "bad"),
                EventChoice("Ignore it — take Grab for now.", 0, 0, 0, -10, 5, 0, "ignored_car", "Grab costs add up. And the car problem won't fix itself.", "bad")
            )
        ),

        GameEvent(
            id = "medical_bill",
            title = "Medical Bill 🏥",
            description = "You woke up feeling terrible. After the clinic visit, the bill comes to RM 280. You didn't budget for this.",
            type = "expense",
            background = "bg_bedroom_night",
            character = "evelyn",
            expression = "worried",
            characterLine = "This is literally why people keeps talking about emergency funds. Are you okay though?",
            choices = listOf(
                EventChoice("Pay from emergency fund / savings.", -280, -280, 0, 5, 0, 10, "used_emergency_fund", "Good thing you had some savings. This is exactly what they're for.", "good"),
                EventChoice("Put it on credit — pay next month.", 0, 0, 280, -5, 0, -5, "medical_debt", "Debt added. This will affect your budget next month.", "bad"),
                EventChoice("Ask family for help.", 0, 0, 0, 0, 0, 5, "family_help", "Family helped out this time. But you can't rely on this every month.", "neutral")
            )
        ),

        GameEvent(
            id = "shopee_sale",
            title = "Shopee 11.11 Sale! 🛍️",
            description = "It's 11.11. Aiden has sent you 47 links. Your cart is already at RM 420. Sale ends in 2 hours.",
            type = "social",
            background = "bg_livingroom_night",
            character = "aiden",
            expression = "happy",
            characterLine = "BRO. Everything is 70% off. This is literally free money. Just buy it. You'll regret it if you don't.",
            choices = listOf(
                EventChoice("Buy everything — treat yourself.", -420, 0, 0, -10, 15, 0, "shopee_splurge", "Cart cleared. Wallet emptied. Aiden is proud. Clara is not.", "bad"),
                EventChoice("Buy only what you need — RM 120.", -120, 0, 0, 5, 5, 0, "shopee_moderate", "Reasonable. You got what you needed and saved the rest.", "neutral"),
                EventChoice("Close the app. Not today.", 0, 0, 0, 10, -10, 0, "resisted_shopee", "Impulse controlled. Clara would approve. Aiden is devastated.", "good")
            )
        ),

        GameEvent(
            id = "salary_bonus",
            title = "Performance Bonus! 🎉",
            description = "Your manager just told you — you're getting a RM 300 performance bonus this month. Unexpected!",
            type = "income",
            background = "bg_office_pantry",
            character = "clara",
            expression = "smile",
            characterLine = "Nice work. Now — don't let this become an excuse to spend more. What are you doing with it?",
            choices = listOf(
                EventChoice("Add it all to savings.", 300, 300, 0, 15, -5, 5, "bonus_saved", "Full bonus saved. Your future self appreciates this.", "good"),
                EventChoice("Split — half savings, half treat.", 300, 150, 0, 5, 5, 5, "bonus_split", "Balanced approach. Saved half, enjoyed half. Clara approves.", "good"),
                EventChoice("Spend it — I earned it.", 300, 0, 0, -10, 15, 0, "bonus_spent", "Spent. Aiden is thrilled. Clara gives you a look.", "bad")
            )
        ),

        GameEvent(
            id = "friend_wedding",
            title = "Friend's Wedding 💍",
            description = "Your university friend is getting married next month. You're invited. Ang pow minimum is RM 150, plus outfit, transport — total around RM 350.",
            type = "social",
            background = "bg_bedroom_night",
            character = "evelyn",
            expression = "happy",
            characterLine = "I'm going too! Should we go together? I'm trying to figure out if I can afford the outfit...",
            choices = listOf(
                EventChoice("Budget for it — attend fully.", -350, 0, 0, 0, 5, 15, "attended_wedding", "You went, you celebrated, you spent wisely. Good memories.", "neutral"),
                EventChoice("Give ang pau only, skip outfit.", -150, 0, 0, 5, 0, 5, "partial_wedding", "Practical. Friend understood. Evelyn went for both of you.", "neutral"),
                EventChoice("Can't afford it — skip entirely.", 0, 0, 0, 0, 0, -10, "skipped_wedding", "Saved money but missed the moment. Evelyn was disappointed.", "bad")
            )
        ),

        GameEvent(
            id = "aiden_repayment",
            title = "Aiden Wants His Money Back 💸",
            description = "Aiden knocks on your door. 'Hey... remember that RM 325 I lent you last month? My credit card bill came in. I kind of need it back.'",
            type = "expense",
            background = "bg_livingroom_night",
            character = "aiden",
            expression = "worried",
            characterLine = "I know the timing is bad. But I'm really struggling this month.",
            choices = listOf(
                EventChoice("Pay him back in full — RM 325.", -325, -325, -325, 10, 15, 0, "repaid_aiden_full", "Debt cleared. Aiden looks genuinely relieved. Your savings took a hit.", "neutral"),
                EventChoice("Pay half now — RM 150, rest next month.", -150, -150, -150, 0, 5, 0, "repaid_aiden_half", "Aiden nods. 'Okay. I trust you.' The remaining RM 175 carries to Month 3.", "bad"),
                EventChoice("Tell him you can't right now.", 0, 0, 0, -5, -15, 0, "avoided_aiden_debt", "Aiden goes quiet. The apartment feels tense. Debt remains.", "bad")
            )
        ),

        GameEvent(
            id = "rent_increase",
            title = "Rent Goes Up 📈",
            description = "Your landlord just sent a message. Rent is increasing by RM 100 next month. Your current budget didn't account for this.",
            type = "expense",
            background = "bg_livingroom_day",
            character = "clara",
            expression = "concern",
            characterLine = "This is why I always say — budget with a buffer. What's your plan?",
            choices = listOf(
                EventChoice("Accept and adjust other categories.", -100, 0, 0, 10, 0, 0, "adjusted_budget", "Mature response. You trimmed wants to cover the increase.", "good"),
                EventChoice("Negotiate with landlord.", 0, 0, 0, 5, 0, 5, "negotiated_rent", "Bold move. Landlord agreed to RM 50 increase instead.", "good"),
                EventChoice("Ignore it for now.", 0, 0, 100, -10, 5, 0, "ignored_rent", "The debt is recorded. This will snowball if you keep avoiding it.", "bad")
            )
        )


    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        gameState = loadGameState()
        currentEvent = pickEvent()

        updateHUD()
        displayEvent()
    }

    private fun pickEvent(): GameEvent {
        val usedEvents = gameState.flags
            .filter { it.startsWith("event_used_") }
            .map { it.removePrefix("event_used_") }

        // Force Aiden repayment in Month 2 if player borrowed
        if (gameState.currentMonth == 2
            && "borrowed_aiden" in gameState.flags
            && "event_used_aiden_repayment" !in gameState.flags) {
            return eventPool.find { it.id == "aiden_repayment" } ?: eventPool.random()
        }

        // Force rent increase consequences in Month 2
        if (gameState.debt > 0 && "debt_warning" !in gameState.flags) {
            return eventPool.find { it.id == "rent_increase" }
                ?: eventPool.filter { it.id !in usedEvents }.randomOrNull()
                ?: eventPool.random()
        }

        val available = eventPool.filter { it.id !in usedEvents }
        return available.randomOrNull() ?: eventPool.random()
    }

    private fun displayEvent() {
        // Set background
        val bgResId = resources.getIdentifier(currentEvent.background, "drawable", packageName)
        if (bgResId != 0) binding.imgBackground.setImageResource(bgResId)

        // Set event type label
        binding.tvEventType.text = when (currentEvent.type) {
            "expense" -> "⚠ UNEXPECTED EXPENSE"
            "income"  -> "🎉 GOOD NEWS"
            "social"  -> "👥 SOCIAL EVENT"
            else      -> "RANDOM EVENT"
        }

        binding.tvEventType.setTextColor(
            when (currentEvent.type) {
                "expense" -> Color.parseColor("#F09595")
                "income"  -> Color.parseColor("#1D9E75")
                "social"  -> Color.parseColor("#AFA9EC")
                else      -> Color.parseColor("#EF9F27")
            }
        )

        // Set event content
        binding.tvEventTitle.text = currentEvent.title
        binding.tvEventDesc.text = currentEvent.description

        // Show cost if expense
        val totalCost = currentEvent.choices.minOfOrNull { it.balanceEffect } ?: 0
        if (totalCost < 0 && currentEvent.type == "expense") {
            binding.tvEventCost.text = "Potential cost: RM ${Math.abs(totalCost)}"
            binding.tvEventCost.visibility = View.VISIBLE
        } else {
            binding.tvEventCost.visibility = View.GONE
        }

        // Show character and their reaction
        showCharacter(currentEvent.character, currentEvent.expression)

        // Show character dialogue after short delay
        handler.postDelayed({
            showCharacterDialogue()
        }, 800)

        // Show choices after dialogue appears
        handler.postDelayed({
            showChoices()
        }, 1500)
    }

    private fun showCharacter(character: String, expression: String) {
        if (character.isEmpty()) {
            binding.imgCharacter.visibility = View.INVISIBLE
            return
        }
        val drawableName = "char_${character}_${expression}"
        val resId = resources.getIdentifier(drawableName, "drawable", packageName)
        if (resId != 0) {
            binding.imgCharacter.setImageResource(resId)
            binding.imgCharacter.visibility = View.VISIBLE
        }
    }

    private fun showCharacterDialogue() {
        if (currentEvent.characterLine.isEmpty()) return

        val speakerName = when (currentEvent.character) {
            "clara"  -> "Ms. Clara"
            "aiden"  -> "Aiden"
            "evelyn" -> "Evelyn"
            else     -> ""
        }

        binding.tvSpeaker.text = speakerName
        binding.tvSpeaker.setTextColor(
            when (currentEvent.character) {
                "clara"  -> Color.parseColor("#1D9E75")
                "aiden"  -> Color.parseColor("#D85A30")
                "evelyn" -> Color.parseColor("#7F77DD")
                else     -> Color.WHITE
            }
        )
        binding.tvSpeaker.visibility = View.VISIBLE
        binding.dialogueBox.visibility = View.VISIBLE
        binding.tvCharacterDialogue.text = currentEvent.characterLine
    }

    private fun showChoices() {
        binding.choicesContainer.removeAllViews()
        binding.choicesContainer.visibility = View.VISIBLE

        currentEvent.choices.forEachIndexed { index, choice ->
            val btn = TextView(this).apply {
                text = choice.text
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(28, 18, 28, 18)
                background = ContextCompat.getDrawable(context, R.drawable.dialogue_bubble_bg)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }

                setOnClickListener {
                    applyChoice(index)
                }
            }
            binding.choicesContainer.addView(btn)
        }
    }

    private fun applyChoice(choiceIndex: Int) {
        val choice = currentEvent.choices[choiceIndex]

        // Apply effects to GameState
        gameState.balance   = (gameState.balance + choice.balanceEffect).coerceAtLeast(0)
        gameState.savings   = (gameState.savings + choice.savingsEffect).coerceAtLeast(0)
        gameState.debt      = (gameState.debt + choice.debtEffect).coerceAtLeast(0)
        gameState.claraRel  = (gameState.claraRel + choice.claraRel).coerceIn(0, 100)
        gameState.aidenRel  = (gameState.aidenRel + choice.aidenRel).coerceIn(0, 100)
        gameState.evelynRel = (gameState.evelynRel + choice.evelynRel).coerceIn(0, 100)

        if (choice.flag.isNotEmpty()) gameState.flags.add(choice.flag)
        gameState.flags.add("event_used_${currentEvent.id}")

        if (currentEvent.id == "rent_increase") {
            when (choiceIndex) {
                0 -> gameState.flags.add("min_rent_850")
                1 -> gameState.flags.add("min_rent_800")
                2 -> gameState.flags.add("min_rent_850")
            }
        }

        saveGameState(gameState)

        // Hide choices, show result
        binding.choicesContainer.visibility = View.GONE

        // Show result in dialogue box
        binding.tvCharacterDialogue.text = choice.resultText
        binding.tvCharacterDialogue.setTextColor(
            when (choice.resultColor) {
                "good"    -> Color.parseColor("#9FE1CB")
                "bad"     -> Color.parseColor("#F09595")
                "neutral" -> Color.WHITE
                else      -> Color.WHITE
            }
        )

        // Update HUD with new balance
        updateHUD()

        // Show continue button
        handler.postDelayed({
            binding.btnContinue.visibility = View.VISIBLE
            binding.btnContinue.setOnClickListener {
                navigateNext()
            }
        }, 500)
    }

    private fun navigateNext() {
        when (gameState.currentMonth) {
            1 -> {
                gameState.currentMonth = 2
                gameState.balance = gameState.balance + 2500  // carry forward + new salary
                gameState.currentSceneId = "act2_intro"
                gameState.currentActFile = "act2_scenes.json"
                saveGameState(gameState)
                val intent = Intent(this, DialogueActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            2 -> {
                gameState.currentMonth = 3
                gameState.balance = gameState.balance + 2500  // carry forward + new salary
                gameState.currentSceneId = "act3_intro"
                gameState.currentActFile = "act3_scenes.json"
                saveGameState(gameState)
                val intent = Intent(this, DialogueActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            3 -> {
                // Game over — go to ending
                saveGameState(gameState)
                val intent = Intent(this, EndingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            else -> {
                val intent = Intent(this, EndingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }

    private fun updateHUD() {
        binding.tvBalance.text = "RM ${"%,d".format(gameState.balance)}"
        binding.tvMonth.text = "Month ${gameState.currentMonth}"
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