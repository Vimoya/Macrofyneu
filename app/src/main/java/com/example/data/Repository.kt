package com.example.data

import kotlinx.coroutines.flow.Flow

class MacrofyRepository(private val dao: MacrofyDao) {

    fun getUserProfileFlow(): Flow<UserProfile?> = dao.getUserProfileFlow()

    suspend fun getUserProfile(): UserProfile? = dao.getUserProfile()

    suspend fun saveUserProfile(profile: UserProfile) = dao.saveUserProfile(profile)

    fun getAllFoodEntriesFlow(): Flow<List<FoodEntry>> = dao.getAllFoodEntriesFlow()

    fun getFoodEntriesForDayFlow(startOfDay: Long, endOfDay: Long): Flow<List<FoodEntry>> =
        dao.getFoodEntriesForDayFlow(startOfDay, endOfDay)

    suspend fun getFoodEntriesForDay(startOfDay: Long, endOfDay: Long): List<FoodEntry> =
        dao.getFoodEntriesForDay(startOfDay, endOfDay)

    suspend fun insertFoodEntry(entry: FoodEntry) = dao.insertFoodEntry(entry)

    suspend fun updateFoodEntry(entry: FoodEntry) = dao.updateFoodEntry(entry)

    suspend fun deleteFoodEntry(entry: FoodEntry) = dao.deleteFoodEntry(entry)

    suspend fun deleteFoodEntryById(id: Int) = dao.deleteFoodEntryById(id)

    fun getWaterLogsForDayFlow(startOfDay: Long, endOfDay: Long): Flow<List<WaterLog>> =
        dao.getWaterLogsForDayFlow(startOfDay, endOfDay)

    suspend fun insertWaterLog(log: WaterLog) = dao.insertWaterLog(log)

    suspend fun removeLastWaterLog(startOfDay: Long, endOfDay: Long) =
        dao.removeLastWaterLog(startOfDay, endOfDay)

    fun getWeightLogsFlow(): Flow<List<WeightLog>> = dao.getWeightLogsFlow()

    suspend fun insertWeightLog(log: WeightLog) = dao.insertWeightLog(log)

    fun getChatMessagesFlow(): Flow<List<ChatMessage>> = dao.getChatMessagesFlow()

    suspend fun insertChatMessage(message: ChatMessage) = dao.insertChatMessage(message)

    suspend fun clearChatHistory() = dao.clearChatHistory()

    fun getAllScannedHistoryFlow(): Flow<List<ScannedFood>> = dao.getAllScannedHistoryFlow()

    suspend fun insertScannedFood(scanned: ScannedFood) = dao.insertScannedFood(scanned)

    suspend fun deleteScannedFood(scanned: ScannedFood) = dao.deleteScannedFood(scanned)

    suspend fun clearScannedHistory() = dao.clearScannedHistory()
}
