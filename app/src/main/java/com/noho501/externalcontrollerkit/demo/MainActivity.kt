package com.noho501.externalcontrollerkit.demo

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.noho501.externalcontrollerkit.manager.ExternalController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var externalController: ExternalController

    private val demoViewModel: DemoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                MainScreen(viewModel = demoViewModel)
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handled = externalController.onKeyEvent(event)
        return handled || super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        val handled = externalController.onMotionEvent(ev)
        return handled || super.dispatchGenericMotionEvent(ev)
    }
}
