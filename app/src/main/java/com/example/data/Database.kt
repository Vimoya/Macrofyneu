package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val gender: String, // "männlich" or "weiblich"
    val age: Int,
    val height: Float, // in cm
    val weight: Float, // in kg
    val goal: String, // "Abnehmen", "Muskelaufbau", "Gewicht halten"
    val activityLevel: String, // "wenig aktiv", "leicht aktiv", "aktiv", "sehr aktiv"
    val pace: String = "",
    val calorieTarget: Int,
    val proteinTarget: Float,
    val carbTarget: Float,
    val fatTarget: Float,
    val bmi: Float,
    val bmr: Float,
    val tdee: Float,
    val completedOnboarding: Boolean = false
)

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val timestamp: Long,
    val mealType: String, // "Frühstück", "Mittagessen", "Abendessen", "Snack"
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val sugar: Float = 0f,
    val portionGrams: Float = 100f,
    val confidenceScore: Int = 95,
    val isPackaging: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val amountMl: Int
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val weight: Float
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val sender: String, // "user" or "assistant"
    val text: String
)

@Entity(tableName = "scanned_history")
data class ScannedFood(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val timestamp: Long,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val sugar: Float = 0f,
    val portionGrams: Float = 100f,
    val confidenceScore: Int = 95,
    val isPackaging: Boolean = false,
    val imagePath: String? = null // Local file path of the saved photo
)

@Dao
interface MacrofyDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAllFoodEntriesFlow(): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp ASC")
    fun getFoodEntriesForDayFlow(startOfDay: Long, endOfDay: Long): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp ASC")
    suspend fun getFoodEntriesForDay(startOfDay: Long, endOfDay: Long): List<FoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(entry: FoodEntry)

    @Update
    suspend fun updateFoodEntry(entry: FoodEntry)

    @Delete
    suspend fun deleteFoodEntry(entry: FoodEntry)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteFoodEntryById(id: Int)

    @Query("SELECT * FROM water_logs WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getWaterLogsForDayFlow(startOfDay: Long, endOfDay: Long): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = (SELECT id FROM water_logs WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY id DESC LIMIT 1)")
    suspend fun removeLastWaterLog(startOfDay: Long, endOfDay: Long)

    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getWeightLogsFlow(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    @Query("SELECT * FROM scanned_history ORDER BY timestamp DESC")
    fun getAllScannedHistoryFlow(): Flow<List<ScannedFood>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedFood(scanned: ScannedFood)

    @Delete
    suspend fun deleteScannedFood(scanned: ScannedFood)

    @Query("DELETE FROM scanned_history")
    suspend fun clearScannedHistory()
}

@Database(entities = [UserProfile::class, FoodEntry::class, WaterLog::class, WeightLog::class, ChatMessage::class, ScannedFood::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): MacrofyDao
}
