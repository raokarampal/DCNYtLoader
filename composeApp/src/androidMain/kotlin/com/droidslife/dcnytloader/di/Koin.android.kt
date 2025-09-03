package com.droidslife.dcnytloader.di

import com.apollographql.apollo.ApolloClient
import com.droidslife.dcnytloader.DownloadNotificationManagerImpl
import com.droidslife.dcnytloader.utils.DownloadNotificationManager
import org.koin.dsl.module

actual fun platformModule() =
    module {
        factory {
            ApolloClient.Builder()
        }
        single<DownloadNotificationManager> {
            DownloadNotificationManagerImpl(get())
        }
    }
