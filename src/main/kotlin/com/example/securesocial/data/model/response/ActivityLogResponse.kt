package com.example.securesocial.data.model.response

import com.example.securesocial.data.model.LogType

data class ActivityLogResponse(
    val id: String,
    val userId: String,
    val action: LogType,
    val createdAt: Long,
    val details: String?
)
