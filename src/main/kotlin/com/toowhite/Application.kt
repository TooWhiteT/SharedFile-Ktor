package com.toowhite

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.toowhite.plugins.*
import io.ktor.http.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.partialcontent.*

fun main() {
    // Auto reload 配置 并执行命令./gradlew -t installDist  开启auto-load需要 监听变更
    embeddedServer(
        Netty,
        port = 8000,
        host = "0.0.0.0",
        module = Application::module,
        watchPaths = listOf("SharedFile-Ktor")
    ).start(wait = true)
}

fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureTemplating()
    configureDatabases()
    configureSockets()
    configureRouting()
    // 一个服务端支持断点续传 解决视频流播放问题
    install(PartialContent) {
        // Maximum number of ranges that will be accepted from a HTTP request.
        // If the HTTP request specifies more ranges, they will all be merged into a single range.
        maxRangeCount = 10
    }
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
        excludeContentType(ContentType.Video.Any)
    }
}
