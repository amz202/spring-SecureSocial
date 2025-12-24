package com.example.securesocial.service

import com.example.securesocial.data.model.response.ActivityLogResponse
import com.example.securesocial.data.repositories.ActivityLogRepository
import org.springframework.stereotype.Service

@Service
class DashService(
    private val activityLogRepository: ActivityLogRepository
) {
    fun getActivityLog(userId: String):List<ActivityLogResponse>{
        val logs = activityLogRepository.findByUserId(userId)

        val response = logs?.sortedByDescending { it.createdAt }?.map { log ->
            ActivityLogResponse(
                id = log.id.toHexString(),
                userId = log.userId,
                action = log.action,
                createdAt = log.createdAt,
                details = log.details
            )
        } ?: emptyList()
        return response
    }
}