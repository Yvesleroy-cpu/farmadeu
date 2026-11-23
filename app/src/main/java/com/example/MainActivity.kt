package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.FarmRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FarmViewModel
import com.example.ui.viewmodel.FarmViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database, DAOs and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FarmRepository(
            database.cropDao(),
            database.livestockDao(),
            database.scanResultDao(),
            database.farmTaskDao(),
            database.inventoryDao()
        )
        
        // Instantiate ViewModel using custom Factory
        val viewModel: FarmViewModel by viewModels {
            FarmViewModelFactory(application, repository)
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
