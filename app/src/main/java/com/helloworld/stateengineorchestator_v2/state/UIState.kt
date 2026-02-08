package com.helloworld.stateengineorchestator_v2.state

sealed class UIState {
    object Idle : UIState()

    object Loading : UIState()

    data class Success(val data: String) : UIState()

    data class Error(val reason: Throwable) : UIState()
}
