package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE payload LIKE '%' || :query || '%' OR source LIKE '%' || :query || '%' OR messageId LIKE '%' || :query || '%' ORDER BY timestamp ASC")
    fun searchMessages(query: String): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE isOutgoing = 0")
    fun getReceivedMessagesCount(): Flow<Int>

    @Query("SELECT * FROM messages WHERE messageId = :msgId LIMIT 1")
    suspend fun getMessageById(msgId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET deliveryStatus = :status WHERE messageId = :msgId")
    suspend fun updateDeliveryStatus(msgId: String, status: String)

    @Query("DELETE FROM messages WHERE messageId = :msgId")
    suspend fun deleteMessageById(msgId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}
