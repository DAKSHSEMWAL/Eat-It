package com.daksh.eatit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daksh.eatit.designsystem.*
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val configured = BuildConfig.DEMO || FirebaseApp.initializeApp(this) != null
        setContent {
            EatItTheme {
                Surface {
                    if (!configured) {
                        EatItEmptyState("Setup required", "This live build isn’t connected to a service yet. Please contact the app administrator.")
                    } else {
                        val model: EatItViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T = EatItViewModel(
                                if (BuildConfig.DEMO) DemoRepository(applicationContext) else FirebaseRepository(),
                                CartStore(applicationContext),
                            ) as T
                        })
                        EatItApp(model)
                    }
                }
            }
        }
    }
}
