package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE isArchived = 0 ORDER BY isPinned DESC, createdDate DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE status = 'UNREAD' AND isArchived = 0 ORDER BY createdDate DESC")
    fun getUnreadNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE status = 'READ' AND isArchived = 0 ORDER BY createdDate DESC")
    fun getReadNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isPinned = 1 AND isArchived = 0 ORDER BY createdDate DESC")
    fun getPinnedNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isArchived = 1 ORDER BY createdDate DESC")
    fun getArchivedNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE status = 'UNREAD' AND isArchived = 0")
    fun getUnreadCountFlow(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: String): NotificationEntity?

    @Query("UPDATE notifications SET status = 'READ', readDate = :readDate WHERE id = :id")
    suspend fun markAsRead(id: String, readDate: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET status = 'READ', readDate = :readDate WHERE status = 'UNREAD'")
    suspend fun markAllAsRead(readDate: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)

    @Query("UPDATE notifications SET isArchived = 1, status = 'READ' WHERE id = :id")
    suspend fun archiveNotification(id: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications WHERE isArchived = 1")
    suspend fun clearArchived()

    @Query("SELECT * FROM notifications WHERE (title LIKE '%' || :query || '%' OR message LIKE '%') AND isArchived = 0")
    fun searchNotificationsFlow(query: String): Flow<List<NotificationEntity>>
}
