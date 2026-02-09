package com.helloworld.stateengineorchestator_v2.state

sealed class UIState {
    object Idle : UIState()

    data class Loading(
        val attempt: Int,
        val maxAttempts: Int,
        val waitingMs: Long? = null
    ) : UIState()

    data class Success(val data: Any) : UIState()

    data class Error(val reason: Throwable) : UIState()
}
