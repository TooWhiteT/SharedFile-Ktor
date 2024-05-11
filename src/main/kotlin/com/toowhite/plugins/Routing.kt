package com.toowhite.plugins

import com.toowhite.bean.SharedDataBean
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Application.configureRouting() {

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val shareDirPath = this::class.java.classLoader.getResource("share")?.file ?: "" // 运行jar编译路径 在项目build文件下

    routing {
        // Static plugin. Try to access `/static/upload.html`
        static("/static") {
            resources("static")
        }
        static("/share") {
            resources("share")
        }

        get("/") {
//            call.respondText("Hello World!")
            val files = File(shareDirPath)
            val list = files.listFiles()?.map {
                SharedDataBean(
                    name = it.name,
                    url = "./share/${it.name}",
                    icon = getTypeIcon(it.name),
                    date = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(
                            Files.readAttributes(
                                it.toPath(),
                                BasicFileAttributes::class.java
                            ).creationTime()
                                .toMillis()
                        ),
                        ZoneId.systemDefault()
                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
            }?.sortedByDescending {
                it.date
            } ?: listOf<String>()
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to list)))
        }

        get("/uploadFile") {
            call.respondRedirect("/static/upload.html")
        }

        post("/upload") {
            val multipart = call.receiveMultipart()
            val targetDir = File(shareDirPath) // 替换为你想保存文件的目录路径

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName ?: "unknown"
                        val file = File(targetDir, fileName)
                        // 将上传的文件保存到目标目录
                        Files.copy(part.streamProvider(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                    else -> {
                        call.respond(HttpStatusCode.BadRequest, "File Error")
                    }
                }
                part.dispose()
            }
            call.respond(HttpStatusCode.OK, "File uploaded successfully")
        }

        get("/getAllFiles") {
            val files = File(shareDirPath)
            val json = mutableListOf<SharedDataBean>()
            files.listFiles()?.map {
                json.add(
                    SharedDataBean(
                        name = it.name,
                        url = "./share/${it.name}",
                        icon = getTypeIcon(it.name),
                        date = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(
                                Files.readAttributes(it.toPath(), BasicFileAttributes::class.java).creationTime().toMillis()
                            ),
                            ZoneId.systemDefault()
                        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                )
            }
            call.respond(json)
        }
    }
}

fun getTypeIcon(fileName: String): String {
    val suffix = fileName.substring(fileName.lastIndexOf(".") + 1).lowercase()
    var type = "./static/type/"
    when (suffix) {
        "apk" -> {
            type += "apk_icon.png"
        }
        "png","jpg" -> {
            type += "pic_icon.png"
        }
        "mp4" -> {
            type += "video_icon.png"
        }
        else -> {
            type += "file_icon.png"
        }
    }
    return type
}
