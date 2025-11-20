package ru.psychologicalTesting.llm.config.cors

import dev.h4kt.ktorDocs.serializers.SerialHttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class SerialCorsConfig(
    override val allowAnyHost: Boolean = false,
    override val allowedHosts: List<CorsConfig.AllowedHost> = emptyList(),
    override val allowAnyMethod: Boolean = true,
    override val allowedMethods: List<SerialHttpMethod> = emptyList(),
    override val allowAnyHeader: Boolean = true,
    override val allowedHeaders: List<String> = emptyList(),
    override val allowSameOrigin: Boolean = false,
    override val allowCredentials: Boolean = true,
    override val allowNonSimpleContentTypes: Boolean = true
) : CorsConfig
