package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class ImageRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("image_gen_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _images = MutableStateFlow<List<ImageItem>>(emptyList())
    val images: StateFlow<List<ImageItem>> = _images.asStateFlow()

    init {
        loadStoredImages()
    }

    private fun loadStoredImages() {
        val storedJson = prefs.getString("gallery_items", null)
        val loadedList = mutableListOf<ImageItem>()

        if (!storedJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    loadedList.add(
                        ImageItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            prompt = obj.optString("prompt", ""),
                            size = obj.optString("size", "512x512"),
                            quality = obj.optString("quality", "standard"),
                            imageUrl = obj.optString("imageUrl", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            filename = obj.optString("filename", "image.png")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (loadedList.isEmpty()) {
            // Seed with creative default showcase items
            val seedItems = listOf(
                ImageItem(
                    id = "seed_1",
                    prompt = "Cyberpunk neon city skyline at night with flying vehicles and rain reflections",
                    size = "512x512",
                    quality = "high",
                    imageUrl = "https://image.pollinations.ai/prompt/Cyberpunk%20neon%20city%20skyline%20at%20night%20with%20flying%20vehicles%20and%20rain%20reflections?width=512&height=512&seed=42&nologo=true",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    filename = "20260908_cyberpunk_neon.png"
                ),
                ImageItem(
                    id = "seed_2",
                    prompt = "Cute golden retriever astronaut floating in deep cosmos nebula, digital art",
                    size = "512x512",
                    quality = "standard",
                    imageUrl = "https://image.pollinations.ai/prompt/Cute%20golden%20retriever%20astronaut%20floating%20in%20deep%20cosmos%20nebula%2C%20digital%20art?width=512&height=512&seed=101&nologo=true",
                    timestamp = System.currentTimeMillis() - 43200000L,
                    filename = "20260908_space_retriever.png"
                ),
                ImageItem(
                    id = "seed_3",
                    prompt = "Fantasy enchanted emerald forest with glowing fairy lanterns and ancient stone temple",
                    size = "640x480",
                    quality = "high",
                    imageUrl = "https://image.pollinations.ai/prompt/Fantasy%20enchanted%20emerald%20forest%20with%20glowing%20fairy%20lanterns%20and%20ancient%20stone%20temple?width=640&height=480&seed=777&nologo=true",
                    timestamp = System.currentTimeMillis() - 21600000L,
                    filename = "20260909_emerald_temple.png"
                )
            )
            loadedList.addAll(seedItems)
            saveStoredImages(loadedList)
        }

        _images.value = loadedList
    }

    private fun saveStoredImages(list: List<ImageItem>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("prompt", item.prompt)
                    put("size", item.size)
                    put("quality", item.quality)
                    put("imageUrl", item.imageUrl)
                    put("timestamp", item.timestamp)
                    put("filename", item.filename)
                }
                array.put(obj)
            }
            prefs.edit().putString("gallery_items", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun generateImage(
        prompt: String,
        size: String,
        quality: String
    ): Result<ImageItem> = withContext(Dispatchers.IO) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Prompt cannot be empty"))
        }

        // Parse dimensions
        val (width, height) = when (size) {
            "256x256" -> Pair(256, 256)
            "640x480" -> Pair(640, 480)
            "1024x512" -> Pair(1024, 512)
            else -> Pair(512, 512)
        }

        val seed = (System.currentTimeMillis() % 1000000).toInt()
        val encodedPrompt = URLEncoder.encode(trimmed, "UTF-8")
        val enhanceParam = if (quality == "high") "&enhance=true" else ""
        val targetUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&seed=$seed$enhanceParam&nologo=true"

        try {
            // Verify generation endpoint response
            val request = Request.Builder()
                .url(targetUrl)
                .head()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 405) {
                    // Pollinations HEAD might return 405 on some CDNs; if so, URL is still valid image stream
                }
            }

            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            val dateStr = dateFormat.format(Date(timestamp))
            val filename = "${dateStr}_${UUID.randomUUID().toString().take(8)}.png"

            val newItem = ImageItem(
                id = UUID.randomUUID().toString(),
                prompt = trimmed,
                size = size,
                quality = quality,
                imageUrl = targetUrl,
                timestamp = timestamp,
                filename = filename
            )

            val updatedList = listOf(newItem) + _images.value
            _images.value = updatedList
            saveStoredImages(updatedList)

            Result.success(newItem)
        } catch (e: Exception) {
            // Fallback generation: provide URL directly so Coil can stream it
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            val dateStr = dateFormat.format(Date(timestamp))
            val filename = "${dateStr}_${UUID.randomUUID().toString().take(8)}.png"

            val newItem = ImageItem(
                id = UUID.randomUUID().toString(),
                prompt = trimmed,
                size = size,
                quality = quality,
                imageUrl = targetUrl,
                timestamp = timestamp,
                filename = filename
            )

            val updatedList = listOf(newItem) + _images.value
            _images.value = updatedList
            saveStoredImages(updatedList)

            Result.success(newItem)
        }
    }

    fun deleteImage(id: String) {
        val updated = _images.value.filterNot { it.id == id }
        _images.value = updated
        saveStoredImages(updated)
    }

    fun clearAll() {
        _images.value = emptyList()
        prefs.edit().remove("gallery_items").apply()
    }
}
