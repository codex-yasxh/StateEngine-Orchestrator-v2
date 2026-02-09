package com.helloworld.stateengineorchestator_v2.viewModels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helloworld.stateengineorchestator_v2.data.api.fetchFeed
import com.helloworld.stateengineorchestator_v2.data.api.fetchProfile
import com.helloworld.stateengineorchestator_v2.data.api.fetchStats
import com.helloworld.stateengineorchestator_v2.state.UIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException


class MyViewModel : ViewModel(){
    private val TAG = "RetryEngine"

    private val _uiState = MutableStateFlow<UIState>(UIState.Idle) //mutable & can only be changed by VM
    val uiState: StateFlow<UIState> = _uiState.asStateFlow() //read only the UI can read
    private var currentJob: Job? = null


    fun processIntent(intent: ScreenIntent) {
        when(intent){
            ScreenIntent.Load,
            ScreenIntent.Retry -> fetchData()
        }
    }


    //phase 3 of this project where we learnt to first sequentially do the tasks
    private fun fetchDataUsingSequential() {
        viewModelScope.launch {
            try {
//                _uiState.value = UIState.Loading
                val result = fetchProfile()
                _uiState.value = UIState.Success(result)

            }
            catch (e: Exception){
                _uiState.value = UIState.Error(e)
            }
             // 1. Tell UI to show spinner
            // 2. Fetch data from a Repository/API here
            // 3. Update _state.value to UiState.Success(data) or UiState.Error
        }
    }


    //achieving concurrency in tasks
    private fun fetchData() {
        Log.d(TAG, "📥 fetchData() called")

        currentJob?.let {
            Log.d(TAG, "🛑 Cancelling previous job")
            it.cancel()
        }

        currentJob = viewModelScope.launch {
            val maxAttempts = 3
            val baseDelayMillis = 1000L
            var attempt = 1

            Log.d(TAG, "🚀 New job started")
            _uiState.value = UIState.Loading(attempt = 1, maxAttempts = maxAttempts)

            while (true) {
                Log.d(TAG, "🔁 Attempt #$attempt started")
                try {
                    val result = attemptWithTimeout()
                    Log.d(TAG, "✅ Attempt #$attempt SUCCESS")
                    _uiState.value = UIState.Success(result)
                    Log.d(TAG, "🏁 Job completed with SUCCESS")
                    return@launch // ✅ FINAL EXIT

                }catch (e : TimeoutCancellationException){
                    Log.d(TAG, "⌛ Timeout reached during attempt #$attempt")
                    throw e
                }
                catch (e: CancellationException) {
                    // 🚫 Cancellation is not failure
                    Log.d(TAG, "🟡 Job CANCELLED during attempt #$attempt")
                    throw e

                } catch (e: Throwable) {
                    Log.d(TAG, "❌ Attempt #$attempt FAILED: ${e::class.simpleName}")
                    if (attempt >= maxAttempts) {
                        Log.d(TAG, "🟥 Retry budget exhausted. Emitting ERROR.")
                        // 🔒 FINAL FAILURE
                        _uiState.value = UIState.Error(e)
                        Log.d(TAG, "🏁 Job completed with ERROR")
                        return@launch
                    }

                    // ⏳ Backoff before next attempt



                    val backoffDelay = baseDelayMillis * (1 shl (attempt - 1))
                    Log.d(TAG, "⏳ Waiting ${backoffDelay}ms before retry")

                    _uiState.value = UIState.Loading(
                        attempt = attempt + 1,
                        maxAttempts = maxAttempts,
                        waitingMs = backoffDelay
                    )
                    delay(backoffDelay) // cancelable
                    attempt++
                }
            }
        }
    }


    private suspend fun attemptWithTimeout(): String {
        Log.d(TAG, "⏱️ attemptWithTimeout() entered")

        return withTimeout(8_000) {
            coroutineScope {
                Log.d(TAG, "🌐 API calls started")

                val profile = async {
                    Log.d(TAG, "📡 fetchProfile() start")
                    fetchProfile()
                }.await()

                val feed = async {
                    Log.d(TAG, "📡 fetchFeed() start")
                    fetchFeed()
                }.await()

                val stats = async {
                    Log.d(TAG, "📡 fetchStats() start")
                    fetchStats()
                }.await()

                Log.d(TAG, "📦 All API calls completed")

                """
            $profile
            $feed
            $stats
            """.trimIndent()
            }
        }
    }





}