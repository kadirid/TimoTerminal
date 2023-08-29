package com.timo.timoterminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.viewModel.MainActivityViewModel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainActivityViewModel : MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainActivityViewModel.viewModelScope.launch {
            mainActivityViewModel.demoEntities.collect {
                //dieser Code wird immer ausgeführt, wenn die Daten vom ViewModel aktualisiert werden
                // Hier kann man dann das UI demenstprechend updaten
                Log.d("DATABASE", "demoEntities changed ")
            }
        }
    }
}