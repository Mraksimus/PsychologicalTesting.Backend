package ru.psychologicalTesting.main.config.cors

import dev.h4kt.ktorDocs.serializers.SerialHttpMethod
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.cors.CORSConfig as CorsPluginConfig

interface CorsConfig {

    /**
     * Whether any host should be allowed
     *
     * Must default to false
     */
    val allowAnyHost: Boolean
    val allowedHosts: List<AllowedHost>

    /**
     * Whether any [HttpMethod][HttpMethod] should be allowed
     *
     * Must default to true
     */
    val allowAnyMethod: Boolean
    val allowedMethods: List<SerialHttpMethod>

    /**
     * Whether any header should be allowed
     *
     * Must default to true
     */
    val allowAnyHeader: Boolean
    val allowedHeaders: List<String>

    /**
     * Whether credentials should be allowed
     *
     * Must default to false
     */
    val allowSameOrigin: Boolean

    /**
     * Whether credentials should be allowed
     *
     * Must default to true
     */
    val allowCredentials: Boolean

    /**
     * Whether non-simple content types should be allowed
     *
     * Must default to true
     *
     * @see [io.ktor.server.plugins.cors.CORSConfig.allowNonSimpleContentTypes]
     */
    val allowNonSimpleContentTypes: Boolean

    @Serializable
    data class AllowedHost(
        val host: String,
        val schemes: List<String>
    )

}

fun CorsPluginConfig.configure(config: CorsConfig) {

    if (config.allowAnyHost) {
        anyHost()
    } else {

        config.allowedHosts.forEach { (host, schemes) ->
            allowHost(host, schemes)
        }

    }

    if (config.allowAnyMethod) {
        HttpMethod.DefaultMethods.forEach(::allowMethod)
    } else {
        config.allowedMethods.forEach(::allowMethod)
    }

    if (config.allowAnyHeader) {
        allowHeaders { true }
    } else {
        config.allowedHeaders.forEach(::allowHeader)
    }

    allowSameOrigin = config.allowSameOrigin
    allowCredentials = config.allowCredentials
    allowNonSimpleContentTypes = config.allowNonSimpleContentTypes

}
