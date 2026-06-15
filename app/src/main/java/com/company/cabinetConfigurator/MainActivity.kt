package com.company.cabinetConfigurator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cabinetconfigurator.di.AppModule
import com.example.cabinetconfigurator.ui.AppRoot
import com.example.cabinetconfigurator.ui.viewmodel.AppViewModel
import com.example.cabinetconfigurator.ui.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val module = AppModule.get(applicationContext)

        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModelFactory(module))
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRoot(vm)
                }
            }
        }
    }
}
