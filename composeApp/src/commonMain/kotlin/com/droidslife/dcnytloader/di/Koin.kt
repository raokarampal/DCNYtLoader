package com.droidslife.dcnytloader.di

import com.droidslife.dcnytloader.downloads.data.YtdlService
import com.droidslife.dcnytloader.downloads.data.YtdlServiceImpl
import com.droidslife.dcnytloader.downloads.ui.MainViewModel
import com.droidslife.dcnytloader.network.YtdlApolloClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = koinConfiguration {
        modules(platformModule(), commonModule())
        appDeclaration()
    }



fun initKoin() = initKoin() { }

fun commonModule() = module {
    single { YtdlApolloClient() }
    single<YtdlService> { YtdlServiceImpl(get()) }
    viewModelOf(::MainViewModel)

}


expect fun platformModule(): Module
