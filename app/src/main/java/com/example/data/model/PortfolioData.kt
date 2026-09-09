package com.example.data.model

data class SkillItem(
    val name: String,
    val category: String,
    val level: String = "Proficient"
)

data class ExperienceItem(
    val title: String,
    val role: String,
    val duration: String,
    val description: String
)

data class ProjectItem(
    val title: String,
    val description: String,
    val category: String,
    val linkText: String = "View Project",
    val demoPrompt: String? = null
)

data class RewardOffer(
    val id: String,
    val title: String,
    val description: String,
    val rewardValue: String,
    val requirement: String,
    val isUnlocked: Boolean = false,
    val unlockCode: String? = null
)

data class ContactMessage(
    val id: String,
    val name: String,
    val email: String,
    val message: String,
    val timestamp: Long,
    val isQueuedOffline: Boolean = false
)
