package com.helloworld.stateengineorchestator_v2.viewModels

import android.R.attr.data
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helloworld.stateengineorchestator_v2.data.api.fetchFeed
import com.helloworld.stateengineorchestator_v2.data.api.fetchProfile
import com.helloworld.stateengineorchestator_v2.data.api.fetchStats
import com.helloworld.stateengineorchestator_v2.state.UIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class MyViewModel : ViewModel(){
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
                _uiState.value = UIState.Loading
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
        //first kill the previous orchestration
        currentJob?.cancel()

        //start the new orchestration
        currentJob = viewModelScope.launch { //single parent

            _uiState.value = UIState.Loading

            try {
                coroutineScope { //grouping of 3 children and if one fails, all fail
                    val profileDeferred = async { fetchProfile() }
                    val feedDeferred = async { fetchFeed() }
                    val statsDeferred = async { fetchStats() }

                    val profile = profileDeferred.await()
                    val feed = feedDeferred.await()
                    val stats = statsDeferred.await()

                    val combinedData = """
                    $profile
                    $feed
                    $stats
                """.trimIndent()

                    _uiState.value = UIState.Success(combinedData)
                }
                _uiState.value = UIState.Success("Data fetched successfully")
            }catch (e : CancellationException){
                throw e
            }
            catch(e : Throwable){
                _uiState.value = UIState.Error(e)
            }

        }
    }


}