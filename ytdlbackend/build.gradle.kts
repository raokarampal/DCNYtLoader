plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.droidslife.dcnytloader"
version = "1.0.0"
application {
    mainClass.set("com.droidslife.dcnytloader.ApplicationKt")

//    val isDevelopment: Boolean = project.ext.has("development")
//    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.json)
    implementation(libs.graphql.kotlin)

//    implementation(project.dependencies.platform(libs.koin.bom))
//    implementation(libs.koin.core)
//    implementation(libs.koin.ktor)
//    implementation(libs.koin.logger.slf4j)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

tasks.named<JavaExec>("run") {
    jvmArgs =
        listOf(
            "-Dconfig.resource=application.conf",
            "-Dconfig.resource=application-dev.conf",
            "-Dio.ktor.development=true",
        )
}
