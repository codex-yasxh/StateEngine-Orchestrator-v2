package com.helloworld.stateengineorchestator_v2.viewModels


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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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
                val result = attemptWithTimeout() // attempt 1
                _uiState.value = UIState.Success(result)

            } catch (e : Throwable){
                try {
                    val retryResult = attemptWithTimeout() //attempt 2
                    _uiState.value = UIState.Success(retryResult)

                } catch (e2 : TimeoutCancellationException){
                    _uiState.value = UIState.Error(e2)
                }
                catch (e2 : CancellationException){
                    throw e2
                }
                catch(e2 : Throwable){
                    _uiState.value = UIState.Error(e2)
                }
            }
        }
    }

    private suspend fun attemptWithTimeout(): String {
        return withTimeout(8_000) {
            coroutineScope {
                val profile = async { fetchProfile() }.await() //left is short form of "val profile = async{ fetchProfile() }  profile.await()"
                val feed = async { fetchFeed() }.await()
                val stats = async { fetchStats() }.await()

                """
            $profile
            $feed
            $stats
            """.trimIndent()
            }
        }
    }




}