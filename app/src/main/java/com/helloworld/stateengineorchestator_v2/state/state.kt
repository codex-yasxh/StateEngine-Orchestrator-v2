package com.helloworld.stateengineorchestator_v2.state

import android.os.Message

sealed class State{
    object Idle : State(),

    object Loading : State(),

    data class Success(val data: String) : State(),

    data class Error(val msg: Message) : State()
}