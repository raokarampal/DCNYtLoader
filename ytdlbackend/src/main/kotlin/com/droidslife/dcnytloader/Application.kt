package com.droidslife.dcnytloader

import com.droidslife.dcnytloader.graphql.schema.VideoDownloadMutation
import com.droidslife.dcnytloader.graphql.schema.VideoDownloadUpdatesSubscription
import com.droidslife.dcnytloader.graphql.schema.VideoInfoQuery
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.defaultGraphQLStatusPages
import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphQLSubscriptionsRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.resolve
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

internal val LOGGER = KtorSimpleLogger("com.droidslife.dcnytloader.Application")

// fun main() {
//    System.setProperty("io.ktor.development", "true")
//    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
//        .start(wait = true)
// }
fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain
        .main(args)

fun Application.module() {
    configureDependencies()
    configureSockets()
    configureStatusPages()
    configureCORS()
    configureGraphQL()
    configureRouting()
}

private fun ApplicationConfig.loadAppConfig(): AppConfig {
    val appSettings = config("app")
    val appConfig =
        AppConfig(
            serverPort = appSettings.property("serverPort").getString().toInt(),
            downloadCategory = appSettings.property("downloadCategory").getString(),
            downloadPath = appSettings.property("downloadPath").getString(),
            remoteIp = appSettings.property("remoteIp").getString(),
            remotePath = appSettings.property("remotePath").getString(),
        )
    LOGGER.info("appConfig$appConfig")

    return appConfig
}

private fun ApplicationConfig.loadCorsConfig(): List<Pair<String, List<String>>> {
    val appSettings = config("cors")
    val corsConfig = appSettings.configList("allowedHosts")
    val cors =
        corsConfig.map {
            val host = it.property("host").getString()
            val schemes = it.property("schemes").getList()
            Pair(host, schemes)
        }
    LOGGER.info("cors$cors")

    return cors
}

private fun Application.configureDependencies() {
    val config = environment.config
    dependencies {
        provide<AppConfig> {
            config.loadAppConfig()
        }

        provide<YtDlpService> {
            YtDlpService(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                },
                resolve(),
            )
        }
    }
}

private fun Application.configureGraphQL() {
    val service: YtDlpService by dependencies
    val config: AppConfig by dependencies
    install(GraphQL) {
        schema {
            packages =
                listOf(
                    "com.droidslife.dcnytloader.graphql.schema",
                    "com.droidslife.dcnytloader.models",
                )
            queries =
                listOf(
                    VideoInfoQuery(service, config),
                )
            mutations =
                listOf(
                    VideoDownloadMutation(service),
                )
            subscriptions =
                listOf(
                    VideoDownloadUpdatesSubscription(service),
                )
        }
        engine {
            introspection {
                enabled = true
            }

//            dataLoaderRegistryFactory = KotlinDataLoaderRegistryFactory(
//                UniversityDataLoader, CourseDataLoader, BookDataLoader
//            )
        }
        server {
            contextFactory = CustomGraphQLContextFactory()
        }
    }
}

private fun Application.configureCORS() {
    val isDev = developmentMode
    val config = environment.config
    LOGGER.info("dev mode:$isDev")
    install(CORS) {
        if (isDev) {
            config.loadCorsConfig().forEach {
                allowHost(it.first, it.second)
            }
            // Allow necessary HTTP methods
            allowMethod(HttpMethod.Options) // Preflight requests
            allowMethod(HttpMethod.Post) // For GraphQL queries/mutations
            allowMethod(HttpMethod.Get) // If introspection or GET queries are used

            // Allow specific headers (customize based on your needs)
            allowHeader(HttpHeaders.ContentType) // Allow Content-Type header
            allowHeader(HttpHeaders.Authorization) // If using authentication
            allowNonSimpleContentTypes = true // Allow non-simple content types if needed

            allowCredentials = false // Set this to false if you don't need cookies or credentials
            allowHeader("X-Requested-With")
            allowHeader("apollographql-client-name")
            allowHeader("apollographql-client-version")

            // Apollo Server (and thus potentially Sandbox interacting with a spec-compliant server)
            // might send this for CSRF protection on GET/multipart requests.
            // While subscriptions are WebSockets, the initial handshake might be affected.
            allowHeader("Apollo-Require-Preflight") // [1]
            maxAgeInSeconds = 3600
        }
    }
}

private fun Application.configureStatusPages() {
    install(StatusPages) {
        defaultGraphQLStatusPages()
    }
}

private fun Application.configureSockets() {
    val json =
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    install(WebSockets) {
        pingPeriod = 1.seconds
        contentConverter = KotlinxWebsocketSerializationConverter(json)
    }
}

private fun Application.configureRouting() {
    routing {
        staticResources("playground", "playground")
        graphQLGetRoute()
        graphQLPostRoute()
        graphQLSubscriptionsRoute("graphql")
        graphiQLRoute()
        graphQLSDLRoute()
    }
}
