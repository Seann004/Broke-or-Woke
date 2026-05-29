# 💸 Broke or Woke?

> A financial literacy serious game for Android — built as a galge (visual novel) where every financial choice has real consequences.

---

## 📖 About

**Broke or Woke?** is an Android serious game that teaches financial literacy through interactive storytelling. Players take on the role of a fresh graduate navigating their first three months of adult life in KL — managing a RM 2,500 monthly salary, handling unexpected expenses, and building relationships with three supporting characters who each represent a different financial philosophy.

The game is classified as a **serious game** — a software application designed using gaming technology and principles for a primary purpose other than pure entertainment. The primary purpose here is **financial education**.

---

## 🎮 Gameplay Loop

The game spans **3 months (acts)**, each consisting of:

1. **Pre-game quiz** — 3 financial literacy baseline questions before the story starts
2. **Dialogue scenes** — story-driven visual novel scenes with branching choices that affect relationship meters
3. **Budget planning** — allocate your monthly salary across rent, food, transport, wants and savings using sliders
4. **Month-end report card** — 50/30/20 breakdown with ✓ ⚠ ✗ ratings, Clara's feedback, savings milestones
5. **Random event** — an unexpected financial event that tests your decisions and has real consequences on your balance

Your choices affect:
- Your **balance and savings** carried forward each month
- Your **debt** accumulated from bad decisions
- Your **relationship meters** with Clara, Aiden and Evelyn
- Which of the **3 endings** you receive at the end of Month 3

---

## 👥 Characters

| Character | Sprite | Role | Financial Philosophy |
|---|---|---|---|
| **Ms. Clara** (Clara Lim, 27) | Pink hair | Colleague, same company 2nd floor · big sister mentor | 50/30/20 rule, emergency fund, pay yourself first |
| **Aiden** (24) | Dark hoodie | Roommate · tempter archetype | Live for today, savings can wait — until it can't |
| **Evelyn** (23) | Blonde | Childhood friend · mirror archetype | Still figuring it out, learning alongside the player |

Each character has a **relationship meter (0–100)** that rises based on whose advice you follow. The character with the highest meter at the end determines your ending.

---

## 🎭 Character Expressions

| Character | Expressions |
|---|---|
| Ms. Clara | neutral, smile, concern, laugh |
| Aiden | neutral, happy, smug, worried |
| Evelyn | neutral, happy, worried, surprised |

---

## 🏆 Endings

| Ending | Trigger condition |
|---|---|
| **The Right Path** | Clara meter highest + score ≥ 70 |
| **Growing Together** | Evelyn meter highest + score ≥ 70 |
| **Getting There** | Score ≥ 50 (any meter) |
| **The Hard Lesson** | Aiden meter highest + score < 50 |
| **Back to Zero** | Score < 50, no dominant relationship |

---

## 🎲 Random Events Pool

| Event | Type | Character |
|---|---|---|
| Car Breakdown | Expense | Aiden |
| Medical Bill | Expense | Evelyn |
| Shopee 11.11 Sale | Social | Aiden |
| Performance Bonus | Income | Ms. Clara |
| Friend's Wedding | Social | Evelyn |
| Rent Increase | Expense | Ms. Clara |
| Aiden Debt Repayment* | Expense | Aiden |

*Aiden repayment only triggers in Month 2 if player borrowed from Aiden in Month 1's car breakdown event.

---

## 📚 Financial Concepts Taught

| Concept | How it appears in game |
|---|---|
| **50/30/20 rule** | Clara introduces it in Act 1 pantry scene; enforced in report card |
| **Emergency fund** | Referenced in medical bill and car breakdown events |
| **Pay yourself first** | Player choice in Act 3 Evelyn reflection scene |
| **Debt snowball** | Aiden's RM 1,200 credit card debt in Act 2 |
| **Impulse spending** | Shopee 11.11 event and Aiden's keyboard temptation |
| **Minimum rent** | Rent slider enforces minimum RM 750, increases if rent event fires |
| **Carry-forward balance** | Savings and balance carry over between all 3 months |
| **Financial score** | 0–100 score based on savings %, debt level and decision flags |
| **Pre/post assessment** | Pre-game quiz baseline stored as flags, compared at ending |
| **Month-end feedback** | Report card shows ✓ ⚠ ✗ against 50/30/20 targets with Clara's advice |
| **Savings milestones** | Achievement banners at RM 500, RM 1,000, 20% rate, debt-free |

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts (FrameLayout / LinearLayout) |
| UI Binding | ViewBinding |
| IDE | Android Studio |
| Minimum SDK | API 24 (Android 7.0) — covers ~99.2% of devices |
| Target SDK | API 35 |
| Build Config | Kotlin DSL (build.gradle.kts) |
| JSON Parsing | Gson 2.10.1 |
| Game State | SharedPreferences (serialised via Gson) |
| Dialogue Engine | Custom `DialogueManager.kt` |
| Story Scripts | JSON files in `assets/dialogue/` |
| Animations | Android ObjectAnimator, AnimatorSet |

---

## 🗂️ Project Structure

```
app/src/main/
├── java/com/example/brokeorwoke/
│   ├── model/
│   │   ├── GameState.kt           — player data, balance, savings, debt, relationships, flags
│   │   └── DialogueModels.kt      — DialogueLine, SceneChoice, DialogueScene data classes
│   ├── engine/
│   │   └── DialogueManager.kt     — loads JSON, steps through scenes, applies choice effects
│   ├── SplashActivity.kt          — animated title screen with falling coins + typewriter
│   ├── NameInputActivity.kt       — player name entry + pre-game quiz overlay (3 questions)
│   ├── DialogueActivity.kt        — main VN screen with character bio overlay on first meeting
│   ├── BudgetActivity.kt          — salary allocation, 5 sliders, minimum enforcement, no-savings warning
│   ├── ReportCardActivity.kt      — month-end 50/30/20 report, Clara feedback, milestones
│   ├── EventActivity.kt           — random event with 3 choices + consequences
│   └── EndingActivity.kt          — final score, relationship meters, character ending
├── res/
│   ├── drawable/
│   │   ├── char_clara_*.png       — 4 expressions (neutral, smile, concern, laugh)
│   │   ├── char_aiden_*.png       — 4 expressions (neutral, happy, smug, worried)
│   │   ├── char_evelyn_*.png      — 4 expressions (neutral, happy, worried, surprised)
│   │   ├── bg_*.png               — background scenes
│   │   └── dialogue_bubble_bg.xml — reusable dark rounded card drawable
│   └── layout/
│       ├── activity_splash.xml
│       ├── activity_name_input.xml  — includes quiz overlay
│       ├── activity_dialogue.xml    — includes bio card overlay
│       ├── activity_budget.xml      — includes no-savings warning overlay
│       ├── activity_report_card.xml
│       ├── activity_event.xml
│       └── activity_ending.xml
└── assets/
    └── dialogue/
        ├── act1_scenes.json       — Month 1: moving in, first salary, meeting characters
        ├── act2_scenes.json       — Month 2: consequences, Aiden's debt, Clara opens up
        └── act3_scenes.json       — Month 3: reflection, character arcs resolve
```

---

## 📱 Screens

| Screen | Key Features |
|---|---|
| **Splash** | Animated BROKE/WOKE title, falling coins, typewriter tagline, gold + purple colour scheme, developer credit |
| **Name Input** | Player enters name, Ms. Clara greeting, pre-game quiz overlay (3 questions with feedback) |
| **Dialogue** | VN screen with character sprites, HUD (balance + month), typewriter text, choices, character bio on first meeting |
| **Budget** | Monthly breakdown (salary + carried savings + debt), 5 sliders, minimum rent/food/transport enforcement, Clara live advice, no-savings warning overlay |
| **Report Card** | Month-end 50/30/20 breakdown with ✓ ⚠ ✗, Clara's personalised feedback, financial fact, savings milestone achievement |
| **Event** | Random event card, character reaction dialogue, 3 choices, result text, consequence applied to GameState |
| **Ending** | Spring outdoor background, character sprite, stats (balance/saved/debt/score), relationship meters, ending dialogue + financial tip, Play Again |

---

## 🔄 Game Flow

```
Splash
  → Name Input + Pre-game Quiz (3 questions)
    → DialogueActivity (act1_scenes.json)
      → BudgetActivity (Month 1)
        → ReportCardActivity (Month 1 report)
          → EventActivity (random event)
            → DialogueActivity (act2_scenes.json)
              → BudgetActivity (Month 2)
                → ReportCardActivity (Month 2 report)
                  → EventActivity (random event)
                    → DialogueActivity (act3_scenes.json)
                      → BudgetActivity (Month 3)
                        → ReportCardActivity (Month 3 report)
                          → EventActivity (random event)
                            → EndingActivity
                              → Splash (Play Again)
```

---

## 💾 GameState Fields

```kotlin
playerName: String          // entered at name input
balance: Int                // current spendable balance (carries forward)
savings: Int                // total accumulated savings
debt: Int                   // total accumulated debt
currentMonth: Int           // 1, 2, or 3
currentSceneId: String      // last scene ID for act resume
currentActFile: String      // which JSON file to load
claraRel: Int               // Clara relationship meter (0-100)
aidenRel: Int               // Aiden relationship meter (0-100)
evelynRel: Int              // Evelyn relationship meter (0-100)
flags: MutableList<String>  // decision history, met_* flags, event_used_* flags,
                            // pre_quiz_q1/2/3_correct/wrong, milestone_* flags
```

---

## 🎨 Assets

- **Character sprites** — free VN sprite packs (consistent anime art style across all 3 characters)
- **Background scenes** — free game asset packs
- **All assets used under free/CC0 license or free for personal/academic use**

---

## 🚀 How to Run

```bash
git clone https://github.com/YOUR_USERNAME/Broke-or-Woke.git
```

1. Open project in Android Studio
2. Wait for Gradle sync to complete
3. Run on emulator (API 24+) or physical Android device with USB debugging enabled

---


*Broke or Woke? — Because nobody taught us this in school.*
