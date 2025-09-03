package com.droidslife.dcnytloader.di


import com.apollographql.apollo.ApolloClient
import com.droidslife.dcnytloader.utils.DownloadNotificationManager
import com.droidslife.dcnytloader.DownloadNotificationManagerImpl
import com.droidslife.dcnytloader.utils.PlatformContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single <PlatformContext>{ PlatformContext.INSTANCE }
    factory {
        ApolloClient.Builder()
    }
    single<DownloadNotificationManager> {
        DownloadNotificationManagerImpl()
    }
}