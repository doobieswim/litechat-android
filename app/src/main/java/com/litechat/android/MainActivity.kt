package com.litechat.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.litechat.android.ui.ChatViewModel
import com.litechat.android.ui.LiteChatRoot
import com.litechat.android.ui.theme.LiteChatTheme

class MainActivity : ComponentActivity() {
    private val vm: ChatViewModel by viewModels {
        val app = application as LiteChatApp
        ChatViewModel.Factory(app.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContent {
            LiteChatTheme {
                LiteChatRoot(vm)
            }
        }
    }
}
