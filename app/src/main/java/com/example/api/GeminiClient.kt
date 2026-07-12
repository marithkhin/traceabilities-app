package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// Retrofit Data Classes matching Gemini REST format
data class GeminiPart(
    val text: String? = null,
    val inlineData: InlineGeminiData? = null
)

data class InlineGeminiData(
    val mimeType: String,
    val data: String
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiResponseFormat(
    val mimeType: String
)

data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiService::class.java)
    }
}

class GeminiClient {

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeCropPhoto(
        bitmap: Bitmap?,
        cropType: String,
        systemInstructions: String,
        promptText: String = "Analyze this crop image and provide a diagnosis in structured JSON matching the specified format."
    ): DiagnosisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Simple safety checks
        val hasRealApiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && !apiKey.startsWith("MY_")
        
        if (!hasRealApiKey || bitmap == null) {
            Log.w("GeminiClient", "Using Simulated AI diagnosis for $cropType (No valid Gemini API key found or photo is missing)")
            // Simulate realistic AI latency
            kotlinx.coroutines.delay(2000)
            return@withContext getSimulatedDiagnosis(cropType)
        }

        try {
            val base64Image = bitmap.toBase64()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = promptText),
                            GeminiPart(inlineData = InlineGeminiData(mimeType = "image/jpeg", data = base64Image))
                        )
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstructions))
                ),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                )
            )

            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from Gemini")

            Log.d("GeminiClient", "Response JSON: $jsonText")
            parseGeminiJson(jsonText, cropType)

        } catch (e: Exception) {
            Log.e("GeminiClient", "Gemini call failed, falling back to simulated diagnosis", e)
            return@withContext getSimulatedDiagnosis(cropType)
        }
    }

    private fun parseGeminiJson(jsonText: String, cropType: String): DiagnosisResult {
        try {
            // Clean response in case the model added markdown blocks
            var cleanJson = jsonText.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json").removeSuffix("```").trim()
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.removePrefix("```").removeSuffix("```").trim()
            }

            val obj = JSONObject(cleanJson)
            val diagnosis = obj.optString("diagnosis", "Leaf/Fruit Spotted Lesion")
            val severity = obj.optString("severity", "moderate")
            val rootCause = obj.optString("root_cause", "High moisture and inadequate ventilation")
            val confidence = obj.optDouble("confidence", 0.80)
            val khmerSummary = obj.optString("khmer_summary", "ដំណាំរបស់អ្នកមានបញ្ហា។ សូមសម្អាតចម្ការ និងប្រើថ្នាំបង្ការ ឬព្យាបាល។")

            val actionsArray = obj.optJSONArray("recommended_actions")
            val actionsList = mutableListOf<ActionRecommend>()
            if (actionsArray != null) {
                for (i in 0 until actionsArray.length()) {
                    val actObj = actionsArray.getJSONObject(i)
                    actionsList.add(
                        ActionRecommend(
                            action = actObj.optString("action"),
                            urgency = actObj.optString("urgency"),
                            productSku = actObj.optString("product_sku").takeIf { it != "null" && it.isNotEmpty() }
                        )
                    )
                }
            }

            return DiagnosisResult(
                diagnosis = diagnosis,
                severity = severity,
                rootCause = rootCause,
                confidence = confidence,
                recommendedActions = actionsList,
                khmerSummary = khmerSummary,
                isSimulated = false
            )
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error parsing response: $jsonText", e)
            return getSimulatedDiagnosis(cropType)
        }
    }

    // High quality offline fallback / simulation
    fun getSimulatedDiagnosis(cropType: String): DiagnosisResult {
        val simulations = if (cropType == "cacao") {
            listOf(
                DiagnosisResult(
                    diagnosis = "Black Pod Disease (Phytophthora pod rot)",
                    severity = "severe",
                    rootCause = "High relative humidity combined with a dense foliage canopy which keeps rain moisture trapped on pod surfaces.",
                    confidence = 0.91,
                    recommendedActions = listOf(
                        ActionRecommend(
                            action = "Harvest infected pods immediately and bury them away from the cocoa trees to prevent fungal spore dispersal.",
                            urgency = "within_2_days",
                            productSku = "TOOL-CACAO-CUTTER"
                        ),
                        ActionRecommend(
                            action = "Apply Ridomil Gold copper-mancozeb fungicide directly onto healthy, developing pods.",
                            urgency = "within_5_days",
                            productSku = "FUNG-CACAO-BLACK"
                        )
                    ),
                    khmerSummary = "ផ្លែកាកាវរបស់អ្នកមានជំងឺរលួយផ្លែខ្មៅ (បង្កដោយផ្សិត)។ សូមកាត់កាកាវដែលរលួយចោលយកទៅកប់ឆ្ងាយពីគល់ និងបាញ់ថ្នាំការពារផ្សិត Ridomil Gold។",
                    isSimulated = true
                ),
                DiagnosisResult(
                    diagnosis = "Cacao Pod Borer Damage",
                    severity = "moderate",
                    rootCause = "Moth eggs laid on the pod skin have hatched. The larvae tunneled into the husk, disrupting mucilage and nutrient supply.",
                    confidence = 0.86,
                    recommendedActions = listOf(
                        ActionRecommend(
                            action = "Implement complete and timely harvesting of all ripe/yellow pods to prevent larvae escaping into soils.",
                            urgency = "within_3_days",
                            productSku = "TOOL-CACAO-CUTTER"
                        ),
                        ActionRecommend(
                            action = "Spray pure organic cold-pressed Neem oil to deter adult moths from laying eggs on pod skin.",
                            urgency = "within_7_days",
                            productSku = "PROT-NEEM-1L"
                        )
                    ),
                    khmerSummary = "មានសត្វល្អិតចោះផ្លែកាកាវ។ សូមប្រមូលផលផ្លែទុំទាំងអស់ឱ្យបានលឿន ដើម្បីបំផ្លាញសំបុកដង្កូវ និងបាញ់ថ្នាំប្រេងស្តៅធម្មជាតិការពារ។",
                    isSimulated = true
                ),
                DiagnosisResult(
                    diagnosis = "Moderate Nitrogen & Shade Lockout",
                    severity = "mild",
                    rootCause = "Excessive shading (over 60% cover ratio) hindering chlorophyll synthesis, coupled with soil nitrogen depletion.",
                    confidence = 0.89,
                    recommendedActions = listOf(
                        ActionRecommend(
                            action = "Prune surrounding shade trees to target a 30-40% dappled sunlight ratio.",
                            urgency = "within_10_days",
                            productSku = null
                        ),
                        ActionRecommend(
                            action = "Apply Cocoa-Boost Organic Fertilizer (12-11-18) around the root drip line.",
                            urgency = "within_14_days",
                            productSku = "FERT-CACAO-POD"
                        )
                    ),
                    khmerSummary = "ស្លឹកកាកាវមានពណ៌លឿងស្រាលដោយសារម្លប់ក្រាស់ពេក និងខ្វះជាតិអាសូតក្នុងដី។ សូមកាត់មែកឈើម្លប់ខ្លះចេញ និងដាក់ជី Cocoa-Boost។",
                    isSimulated = true
                )
            )
        } else {
            listOf(
                DiagnosisResult(
                    diagnosis = "Anthracnose Fungal Infection (Early Stage)",
                    severity = "moderate",
                    rootCause = "Prolonged leaf moisture and low sunlight transmission due to dense overlapping branches in the canopy.",
                    confidence = 0.88,
                    recommendedActions = listOf(
                        ActionRecommend(
                            action = "Prune and clear deadwood and affected branches immediately to improve air circulation.",
                            urgency = "within_3_days",
                            productSku = "TOOL-PRUNER-LONG"
                        ),
                        ActionRecommend(
                            action = "Apply Kocide 3000 copper-based fungicide to the foliage.",
                            urgency = "within_7_days",
                            productSku = "FUNG-COPPER-1KG"
                        )
                    ),
                    khmerSummary = "ដើមស្វាយចន្ទីរបស់អ្នកមានជំងឺផ្សិតអង់ត្រាក់ណូស (ដំណាក់កាលដំបូង)។ សូមកាត់មែកចាស់ៗឆាប់ៗ ដើម្បីបង្កើនខ្យល់ចេញចូល និងបាញ់ថ្នាំការពារផ្សិតស្ពាន់។",
                    isSimulated = true
                ),
                DiagnosisResult(
                    diagnosis = "Zinc & Boron Micronutrient Deficiency",
                    severity = "mild",
                    rootCause = "High alkaline soils causing lockout of key catalytic microelements required for robust flower bud formation.",
                    confidence = 0.92,
                    recommendedActions = listOf(
                        ActionRecommend(
                            action = "Apply Kenkoshoku Micronutrient Spray (Zn + B + Mg) during pre-flower initiation stage.",
                            urgency = "within_14_days",
                            productSku = "KEN-MICRO-1L"
                        )
                    ),
                    khmerSummary = "ដំណាំចន្ទីខ្វះជាតិស័ង្កសី និងបូរ៉ុង។ សូមបាញ់ទឹកបំប៉នមីក្រូសារធាតុ Kenkoshoku ដើម្បីជំនួយផ្កា និងផ្លែចន្ទី។",
                    isSimulated = true
                ),
                DiagnosisResult(
                    diagnosis = "Cashew Shoot Borer Infestation",
                    severity = "severe",
                    rootCause = "Eggs laid by shoot borer beetles at leaf axils have hatched. Larvae are tunneling inside succulent twigs.",
                    confidence = 0.85,
                    recommendedActions = listOf(
                        ActionRecommend(
                            action = "Cut off infested shoots 10cm below visible exit holes and burn them immediately.",
                            urgency = "within_2_days",
                            productSku = null
                        ),
                        ActionRecommend(
                            action = "Apply organic Neem oil crop protection spray to repel adult beetles and prevent re-laying.",
                            urgency = "within_5_days",
                            productSku = "PROT-NEEM-1L"
                        )
                    ),
                    khmerSummary = "មានសត្វល្អិតដង្កូវស៊ីធ្លុះពន្លកស្វាយចន្ទីធ្ងន់ធ្ងរ។ សូមកាត់មែកដែលមានដង្កូវចេញយកទៅដុតចោល និងបាញ់ថ្នាំប្រេងស្តៅការពារ។",
                    isSimulated = true
                )
            )
        }
        // Pick one at random for mock variety
        return simulations.random()
    }
}

data class DiagnosisResult(
    val diagnosis: String,
    val severity: String,
    val rootCause: String,
    val confidence: Double,
    val recommendedActions: List<ActionRecommend>,
    val khmerSummary: String,
    val isSimulated: Boolean = false
)

data class ActionRecommend(
    val action: String,
    val urgency: String,
    val productSku: String? = null
)
