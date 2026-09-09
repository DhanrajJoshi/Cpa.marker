package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ContactMessage
import com.example.data.model.ExperienceItem
import com.example.data.model.ProjectItem
import com.example.data.model.RewardOffer
import com.example.data.model.SkillItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PortfolioRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("portfolio_prefs", Context.MODE_PRIVATE)

    private val _messages = MutableStateFlow<List<ContactMessage>>(emptyList())
    val messages: StateFlow<List<ContactMessage>> = _messages.asStateFlow()

    private val _rewards = MutableStateFlow<List<RewardOffer>>(emptyList())
    val rewards: StateFlow<List<RewardOffer>> = _rewards.asStateFlow()

    init {
        loadMessages()
        initRewards()
    }

    val skills: List<SkillItem> = listOf(
        SkillItem("HTML & CSS", "Frontend", "Expert"),
        SkillItem("JavaScript & TypeScript", "Frontend", "Advanced"),
        SkillItem("Responsive Design", "UI/UX", "Expert"),
        SkillItem("AI Tools & Automation", "AI & ML", "Advanced"),
        SkillItem("Landing Pages", "Marketing", "Expert"),
        SkillItem("CPA Marketing & Monetization", "Marketing", "Specialist"),
        SkillItem("Python & FastAPI", "Backend", "Proficient"),
        SkillItem("Kotlin & Jetpack Compose", "Mobile", "Advanced")
    )

    val experiences: List<ExperienceItem> = listOf(
        ExperienceItem(
            title = "Web Developer",
            role = "Full-stack & Mobile Architect",
            duration = "2023 - Present",
            description = "Built modern, responsive web applications, PWA frameworks, and automation pipelines for digital projects."
        ),
        ExperienceItem(
            title = "AI Tools Content Creator",
            role = "Creator & Researcher",
            duration = "2022 - Present",
            description = "Created in-depth guides, scripts, video tutorials, and workflows exploring AI generative models and earning strategies."
        ),
        ExperienceItem(
            title = "CPA Marketing Specialist",
            role = "Traffic & Conversion Optimizer",
            duration = "2021 - Present",
            description = "Architected high-converting landing pages, rewards gateways, and traffic funnels with integrated tracking."
        )
    )

    val projects: List<ProjectItem> = listOf(
        ProjectItem(
            title = "Gaming Rewards Landing Page",
            description = "High-conversion responsive landing hub for gamers to unlock rewards, redeem promo codes, and complete partner tasks.",
            category = "CPA & Rewards",
            linkText = "Open Offer Flow",
            demoPrompt = "Epic gaming rewards loot chest with golden coins, cyber crystals and futuristic neon hologram particles"
        ),
        ProjectItem(
            title = "AI Avatar Generator",
            description = "Web and mobile application creating stylized digital avatars and custom personas for content creators and streamers.",
            category = "AI Tools",
            linkText = "Generate Avatar",
            demoPrompt = "Cyberpunk character avatar portrait with holographic glasses and vibrant violet lighting, 3d octane render"
        ),
        ProjectItem(
            title = "Video to Text AI Tool",
            description = "Fast transcription suite converting video and voice into accurate timestamped transcripts and structured summaries.",
            category = "Productivity",
            linkText = "Try Transcribe",
            demoPrompt = "Sound waves transforming into floating glowing typography and sentences, high-tech minimalist visualization"
        ),
        ProjectItem(
            title = "Text → Image Studio",
            description = "Lightweight generative art tool with real-time prompt styling, custom aspect ratios, and local offline gallery.",
            category = "Creative AI",
            linkText = "Create Artwork",
            demoPrompt = "Surreal floating island with cascading waterfalls and pastel aurora borealis sky"
        )
    )

    private fun initRewards() {
        val unlockedSet = prefs.getStringSet("unlocked_rewards", emptySet()) ?: emptySet()
        val defaultRewards = listOf(
            RewardOffer(
                id = "cpa_reward_1",
                title = "🔥 Exclusive Creator Rewards Pack",
                description = "Unlock 50+ curated premium AI prompts, high-resolution background textures, and design templates.",
                rewardValue = "$49 Value",
                requirement = "Complete a quick sponsor check",
                isUnlocked = unlockedSet.contains("cpa_reward_1"),
                unlockCode = if (unlockedSet.contains("cpa_reward_1")) "CREATOR-VIP-779" else null
            ),
            RewardOffer(
                id = "cpa_reward_2",
                title = "🎮 Gamer VIP Loot Pass",
                description = "Get exclusive promo codes and digital bonus assets for top trending mobile games.",
                rewardValue = "$25 Value",
                requirement = "Verify email or install sponsor app",
                isUnlocked = unlockedSet.contains("cpa_reward_2"),
                unlockCode = if (unlockedSet.contains("cpa_reward_2")) "GAMER-BONUS-992" else null
            ),
            RewardOffer(
                id = "cpa_reward_3",
                title = "🚀 CPA Marketing Blueprint 2026",
                description = "Complete guide to setting up high-conversion landing pages and ethical traffic generation.",
                rewardValue = "$99 Value",
                requirement = "Answer a 3-question survey",
                isUnlocked = unlockedSet.contains("cpa_reward_3"),
                unlockCode = if (unlockedSet.contains("cpa_reward_3")) "CPA-GROWTH-2026" else null
            )
        )
        _rewards.value = defaultRewards
    }

    fun unlockReward(id: String) {
        val unlockedSet = prefs.getStringSet("unlocked_rewards", emptySet())?.toMutableSet() ?: mutableSetOf()
        unlockedSet.add(id)
        prefs.edit().putStringSet("unlocked_rewards", unlockedSet).apply()

        _rewards.value = _rewards.value.map { reward ->
            if (reward.id == id) {
                val code = when (id) {
                    "cpa_reward_1" -> "CREATOR-VIP-779"
                    "cpa_reward_2" -> "GAMER-BONUS-992"
                    else -> "CPA-GROWTH-2026"
                }
                reward.copy(isUnlocked = true, unlockCode = code)
            } else {
                reward
            }
        }
    }

    private fun loadMessages() {
        val storedJson = prefs.getString("contact_messages", null)
        val list = mutableListOf<ContactMessage>()
        if (!storedJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ContactMessage(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            email = obj.getString("email"),
                            message = obj.getString("message"),
                            timestamp = obj.getLong("timestamp"),
                            isQueuedOffline = obj.optBoolean("isQueuedOffline", false)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _messages.value = list
    }

    fun submitContactMessage(name: String, email: String, message: String): ContactMessage {
        val isOffline = false // Local queueing ensures messages are never lost
        val newMessage = ContactMessage(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            email = email.trim(),
            message = message.trim(),
            timestamp = System.currentTimeMillis(),
            isQueuedOffline = isOffline
        )

        val updated = listOf(newMessage) + _messages.value
        _messages.value = updated

        try {
            val array = JSONArray()
            for (m in updated) {
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("email", m.email)
                    put("message", m.message)
                    put("timestamp", m.timestamp)
                    put("isQueuedOffline", m.isQueuedOffline)
                }
                array.put(obj)
            }
            prefs.edit().putString("contact_messages", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return newMessage
    }
}
