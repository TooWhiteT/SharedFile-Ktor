package com.toowhite

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.toowhite.plugins.*

fun main() {
    embeddedServer(
        Netty,
        port = 8000,
        host = "0.0.0.0",
        module = Application::module,
        watchPaths = listOf("SharedFile-Ktor") // Auto reload 配置 并执行命令./gradlew -t installDist  开启auto-load需要 监听变更
    ).start(wait = true)
}

fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureTemplating()
    configureDatabases()
    configureSockets()
    configureRouting()
}
