package com.helloworld.stateengineorchestator_v2.data.api

import kotlinx.coroutines.delay
import kotlin.random.Random

//designing fake APIs using suspend function

suspend fun fetchProfile(): String {
    delay(2000)
    val successFetchProfile = Random.nextBoolean()
    if(successFetchProfile) return "Profile fetched successfully"
    else{
        throw Exception("Failed to fetch profile")
    }
}

suspend fun fetchFeed() : String {
    delay(5000)
    val successFetchFeed = Random.nextBoolean()
    if(successFetchFeed) return "Feed fetched successfully"
    else{
        throw Exception("Failed to fetch feed")
    }
}

suspend fun fetchStats() : String {
    delay(7000)
    val successFetchStats = Random.nextBoolean()
    if(successFetchStats) return "Stats fetched successfully"
    else{
        throw Exception("Failed to fetch stats")
    }
}

