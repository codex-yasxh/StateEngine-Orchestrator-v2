package com.helloworld.stateengineorchestator_v2.viewModels

sealed class ScreenIntent{
    object Load : ScreenIntent(),
    object Retry : ScreenIntent()
}