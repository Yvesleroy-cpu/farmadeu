package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    /**
     * Helper to safely execute a Gemini API query
     */
    suspend fun queryGemini(prompt: String, systemInstruction: String? = null, imageBase64: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Note: Clé de l'API Gemini non configurée. Veuillez ajouter votre clé 'GEMINI_API_KEY' dans le panneau Secrets de l'AI Studio pour l'activer.\n\nVoici une réponse d'assistance locale :\nSur la base de votre description, il est fortement possible d'être confronté à une pathologie courante. Assurez-vous d'isoler le sujet malade, d'utiliser un désinfectant approprié et d'éviter l'arrosage ou l'alimentation excessive en attendant le traitement officiel."
        }

        val parts = mutableListOf<Part>()
        parts.add(Part(text = prompt))
        if (imageBase64 != null) {
            parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = imageBase64)))
        }

        val sysPart = systemInstruction?.let { Content(listOf(Part(text = it))) }

        val request = GeminiRequest(
            contents = listOf(Content(parts)),
            generationConfig = GenerationConfig(temperature = 0.3f),
            systemInstruction = sysPart
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Aucune réponse n'a été générée par l'IA."
        } catch (e: Exception) {
            "Une erreur est survenue lors de la communication avec l'IA: ${e.localizedMessage ?: e.message}"
        }
    }
}
