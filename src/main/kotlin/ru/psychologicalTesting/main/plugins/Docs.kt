package ru.psychologicalTesting.main.plugins

import dev.h4kt.ktorDocs.plugin.KtorDocs
import io.ktor.server.application.Application
import io.ktor.server.application.install

fun Application.configureDocs() = install(KtorDocs) {

    openApi {

        version = "3.0.0"

        info {
            version = "1"
            title = "Psychological testing"
        }

        server {
            url = "https://psychological-testing.mraksimus.ru"
            description = "Remote development server"
        }
        server {
            url = "http://localhost:1488"
            description = "Local development server"
        }

    }

    swagger {
        path = "/docs"
    }

}
