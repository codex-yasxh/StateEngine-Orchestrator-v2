package com.helloworld.stateengineorchestator_v2.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helloworld.stateengineorchestator_v2.state.UIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyViewModel : ViewModel(){
    private val _uiState = MutableStateFlow<UIState>(UIState.Idle) //mutable & can only be changed by VM
    val uiState: StateFlow<UIState> = _uiState.asStateFlow() //read only the UI can read

    fun processIntent(intent: ScreenIntent) {
        when(intent){
            ScreenIntent.Load,
            ScreenIntent.Retry -> fetchData()
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading // 1. Tell UI to show spinner
            // 2. Fetch data from a Repository/API here
            // 3. Update _state.value to UiState.Success(data) or UiState.Error
        }
    }
}