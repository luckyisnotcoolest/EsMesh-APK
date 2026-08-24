package com.example.data.repository

import com.example.data.local.dao.MessageDao
import com.example.data.local.entity.MessageEntity
import com.example.data.model.EsMeshMessage
import com.example.data.model.MessageDeliveryStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepository(private val messageDao: MessageDao) {

    val allMessages: Flow<List<EsMeshMessage>> = messageDao.getAllMessagesFlow().map { list ->
        list.map { it.toDomain() }
    }

    val receivedCount: Flow<Int> = messageDao.getReceivedMessagesCount()

    fun search(query: String): Flow<List<EsMeshMessage>> {
        return messageDao.searchMessages(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insertMessage(message: EsMeshMessage): Long {
        return messageDao.insertMessage(MessageEntity.fromDomain(message))
    }

    suspend fun updateStatus(msgId: String, status: MessageDeliveryStatus) {
        messageDao.updateDeliveryStatus(msgId, status.name)
    }

    suspend fun deleteMessage(msgId: String) {
        messageDao.deleteMessageById(msgId)
    }

    suspend fun clearAll() {
        messageDao.clearAllMessages()
    }
}
