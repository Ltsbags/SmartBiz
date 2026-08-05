package com.example.repositories

import com.example.core.database.dao.NotificationDao
import com.example.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val notificationDao: NotificationDao
) {

    val allNotificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    val unreadNotificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getUnreadNotificationsFlow()
    val readNotificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getReadNotificationsFlow()
    val pinnedNotificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getPinnedNotificationsFlow()
    val archivedNotificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getArchivedNotificationsFlow()
    val unreadCountFlow: Flow<Int> = notificationDao.getUnreadCountFlow()

    suspend fun addNotification(notification: NotificationEntity): Long {
        return notificationDao.insertNotification(notification)
    }

    suspend fun addNotifications(notifications: List<NotificationEntity>) {
        notificationDao.insertNotifications(notifications)
    }

    suspend fun updateNotification(notification: NotificationEntity) {
        notificationDao.updateNotification(notification)
    }

    suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun togglePin(id: String, isPinned: Boolean) {
        notificationDao.togglePin(id, isPinned)
    }

    suspend fun archiveNotification(id: String) {
        notificationDao.archiveNotification(id)
    }

    suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }

    suspend fun clearArchived() {
        notificationDao.clearArchived()
    }

    fun searchNotifications(query: String): Flow<List<NotificationEntity>> {
        return notificationDao.searchNotificationsFlow(query)
    }

    suspend fun getNotificationById(id: String): NotificationEntity? {
        return notificationDao.getNotificationById(id)
    }
}
