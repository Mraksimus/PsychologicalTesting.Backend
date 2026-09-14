package ru.psychologicalTesting.main.compat.docs.converters

import dev.h4kt.ktorDocs.generation.converters.auth.AuthProviderConverter
import dev.h4kt.ktorDocs.types.openapi.components.OpenApiSecurityScheme
import io.ktor.server.application.Application
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.jwt.JWTAuthenticationProvider

class JwtAuthConverter : AuthProviderConverter(1000) {

    override fun canConvert(provider: AuthenticationProvider): Boolean {
        return provider is JWTAuthenticationProvider
    }

    override fun convert(
        provider: AuthenticationProvider,
        application: Application
    ) = OpenApiSecurityScheme.Http(OpenApiSecurityScheme.Http.Scheme.BEARER)

}
