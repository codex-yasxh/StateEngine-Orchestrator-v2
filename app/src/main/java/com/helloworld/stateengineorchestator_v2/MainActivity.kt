package com.helloworld.stateengineorchestator_v2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.helloworld.stateengineorchestator_v2.state.UIState
import com.helloworld.stateengineorchestator_v2.ui.theme.StateEngineOrchestatorv2Theme
import com.helloworld.stateengineorchestator_v2.viewModels.MyViewModel
import com.helloworld.stateengineorchestator_v2.viewModels.ScreenIntent
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.TimeoutCancellationException

private const val UI_TAG = "UIStateObserver"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MyViewModel = viewModel()
            MainScreen(viewModel)
        }
    }
}


@Composable
fun MainScreen(viewModel: MyViewModel) {

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (val state = uiState) {

            is UIState.Idle -> {
                Text("Idle state")
            }

            is UIState.Loading -> {
                CircularProgressIndicator()
                Text(
                    "Loading… attempt ${state.attempt}/${state.maxAttempts}"
                )
            }

            is UIState.Success -> {

                Text("Success")
            }

            is UIState.Error -> {
                // 🚫 NO spinner here
                Text("Failed")

                if (state.reason is TimeoutCancellationException) {
                    Text("Request timed out after multiple attempts")
                } else {
                    Text(state.reason.message ?: "Unknown error")
                }
            }
        }


        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ){
            Button(
                onClick = {
                    Log.d(UI_TAG, "👉 User clicked Load / Retry")
                    viewModel.processIntent(ScreenIntent.Load)
                }
            ) {
                Text("Load / Retry")
            }
        }

    }
}

@Preview
@Composable
fun MainScreenPreview() {
    StateEngineOrchestatorv2Theme {
        MainScreen(viewModel = MyViewModel())
    }
}
