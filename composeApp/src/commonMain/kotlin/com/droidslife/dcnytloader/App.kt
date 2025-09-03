package com.droidslife.dcnytloader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.droidslife.dcnytloader.di.initKoin
import com.droidslife.dcnytloader.downloads.ui.download.history.DownloadHistoryScreen
import com.droidslife.dcnytloader.theme.AppTheme
import okio.FileSystem
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App(navController: NavHostController = rememberNavController()) {
    KoinMultiplatformApplication(config = initKoin()) {
        AppTheme {
            setSingletonImageLoaderFactory { context ->
                ImageLoader
                    .Builder(context)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCache {
                        MemoryCache
                            .Builder()
                            .maxSizePercent(context, 0.25)
                            .build()
                    }.diskCache {
                        DiskCache
                            .Builder()
                            .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
                            .maxSizeBytes(50L * 1024 * 1024)
                            .build()
                    }.build()
            }
            DcnYtLoaderApp(navController)
        }
    }
}

@Composable
fun DcnYtLoaderApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = Home) {
        composable<Home>(
            deepLinks =
                listOf(
                    navDeepLink {
                        action = "android.intent.action.SEND"
                        mimeType = "text/*"
                    },
                    navDeepLink {
                        uriPattern = "app://share?text={text}"
                        action = "android.intent.action.SEND"
                        mimeType = "text/*"
                    },
                    navDeepLink {
                        action = "android.intent.action.SEND"
                        mimeType = "image/*"
                    },
                ),
        ) {
            Column(
                Modifier.fillMaxSize(),
            ) {
                DownloadHistoryScreen()
            }
        }
    }
}
