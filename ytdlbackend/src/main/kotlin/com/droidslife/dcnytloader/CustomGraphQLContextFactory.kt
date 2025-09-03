package com.droidslife.dcnytloader

import com.expediagroup.graphql.generator.extensions.plus
import com.expediagroup.graphql.server.ktor.DefaultKtorGraphQLContextFactory
import graphql.GraphQLContext
import io.ktor.server.request.ApplicationRequest

/**
 * Custom logic for how this example app should create its context given the [ApplicationRequest]
 */
class CustomGraphQLContextFactory : DefaultKtorGraphQLContextFactory() {
    override suspend fun generateContext(request: ApplicationRequest): GraphQLContext =
        super.generateContext(request).plus(
            mutableMapOf<Any, Any>(
                "user" to "",
            ).also { map ->
                request.headers["my-custom-header"]?.let { customHeader ->
                    map["customHeader"] = customHeader
                }
            },
        )
}
