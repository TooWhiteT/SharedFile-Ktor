package com.toowhite.bean

import kotlinx.serialization.Serializable

@Serializable
data class SharedDataBean(
    val url: String,
    val name: String,
    val icon: String,
    val date: String
)
