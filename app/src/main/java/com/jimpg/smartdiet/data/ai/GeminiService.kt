package com.jimpg.smartdiet.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.jimpg.smartdiet.BuildConfig
import com.jimpg.smartdiet.data.model.MealAnalysisResponse
import kotlinx.serialization.json.Json
import com.jimpg.smartdiet.data.local.entity.MealEntity
import com.jimpg.smartdiet.data.local.entity.UserProfileEntity
import java.util.Locale

class GeminiService {
    private val json = Json { ignoreUnknownKeys = true }
    
    private val model = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    suspend fun analyzeMeal(
        userDescription: String,
        userProfile: UserProfileEntity?,
        mealHistory: List<MealEntity>
    ): Result<MealAnalysisResponse> {
        return try {
            val userLanguage = Locale.getDefault().displayLanguage
            
            val profileContext = if (userProfile != null) {
                """
                User Profile:
                - Name: ${userProfile.nickname}
                - Age: ${userProfile.age}
                - Gender: ${userProfile.gender}
                - Activity Level Factor: ${userProfile.activityLevel}
                - BMI: ${userProfile.bmi}
                - Calculated Daily Calorie Target (Weight Loss): ${userProfile.calorieTarget} kcal
                - Health Conditions: ${userProfile.healthConditions}
                """
            } else {
                "User Profile: Not set."
            }

            val historyContext = if (mealHistory.isNotEmpty()) {
                val historyString = mealHistory.joinToString("\n") { 
                    "- ${it.description} (${it.calories} kcal, ${it.protein}g protein)" 
                }
                """
                Recent Meal History (Last 20 meals):
                $historyString
                
                IMPORTANT: Analyze this history to identify NUTRITIONAL GAPS. 
                For example, if the user hasn't eaten fish recently, mention the lack of Omega-3. 
                If they lack vegetables, mention fiber/vitamins.
                """
            } else {
                "Recent Meal History: No recorded meals yet."
            }

            val prompt = """
                You are an Expert Clinical Nutritionist. Your goal is to help the user lose weight safely and improve their overall health.
                
                $profileContext
                
                $historyContext
                
                CURRENT MEAL TO ANALYZE: "$userDescription"
                
                INSTRUCTIONS:
                1. Analyze the current meal for calories and macros.
                2. Provide a sophisticated, professional, yet easy-to-understand analysis.
                3. **Contextual Advice:** Combine the current meal analysis with the user's profile and recent history. 
                   - If they have a condition (e.g., Diabetes), warn them about sugar/carbs.
                   - If they haven't eaten a specific nutrient group recently (based on history), advise them to include it in their next meal.
                   - Mention if this meal fits well within their daily calorie target.
                4. **CRITICAL:** The response MUST be in **$userLanguage**.
                
                Return the response in the following JSON format:
                {
                    "description": "Refined name of the meal",
                    "calories": 500,
                    "protein": 30.5,
                    "carbs": 40.0,
                    "fat": 15.0,
                    "analysis": "Your professional analysis here..."
                }
            """.trimIndent()

            val response = model.generateContent(prompt)
            val responseText = response.text ?: return Result.failure(Exception("No response text"))
            
            val analysis = json.decodeFromString<MealAnalysisResponse>(responseText)
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}