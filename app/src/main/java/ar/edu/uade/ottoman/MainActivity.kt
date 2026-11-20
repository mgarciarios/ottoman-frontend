package ar.edu.uade.ottoman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import ar.edu.uade.ottoman.presentation.OttomanViewModel
import ar.edu.uade.ottoman.presentation.ui.OttomanApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[OttomanViewModel::class.java]
        setContent { OttomanApp(viewModel) }
    }
}
