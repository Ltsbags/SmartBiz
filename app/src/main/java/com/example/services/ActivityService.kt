package com.example.services

import com.example.repositories.ActivityRepository
import com.example.repositories.UnifiedActivityItem
import kotlinx.coroutines.flow.Flow

class ActivityService(
    private val activityRepository: ActivityRepository
) {

    fun getActivityTimelineFlow(): Flow<List<UnifiedActivityItem>> {
        return activityRepository.getUnifiedActivityTimelineFlow()
    }
}
