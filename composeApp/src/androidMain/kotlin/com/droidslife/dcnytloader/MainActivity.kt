package com.droidslife.dcnytloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.droidslife.dcnytloader.utils.SharedContent
import com.droidslife.dcnytloader.utils.parseSharedContent
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {
//    private val sharedContentFlow = MutableSharedFlow<SharedContent>(extraBufferCapacity = 1)
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberNavController()
            App(navController)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println(intent.flags)
        println(intent.toUri(0).toString())
//        sharedContentFlow.tryEmit(intent.parseSharedContent(this))
        navController.handleDeepLink(intent)
    }
}

@Preview
@Composable
private fun AppAndroidPreview() {
    App(rememberNavController())
}
