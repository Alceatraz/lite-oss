package top.btswork.liteoss.service

import io.ktor.http.ContentType.Application.OctetStream
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.put
import top.btswork.liteoss.Except
import top.btswork.liteoss.auxiliary.asRoot
import top.btswork.liteoss.dataFolder
import top.btswork.liteoss.logger
import top.btswork.liteoss.service.Service.save
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory

object HandleOssData {

  fun Route.putOssData() = put("/{name...}") {
    val name = call.parameters.getAll("name") ?: throw Except.of(BadRequest, "Usage: PUT /oss/data/{name...}")
    val file = dataFolder.asRoot(name)
    Files.createDirectories(file.parent)
    val exists = Files.deleteIfExists(file)
    call.request.receiveChannel().save(file)
    val status = if (exists) {
      logger.info("[OSS/DATA] PUT REWRITE[202] {}", file)
      Accepted
    } else {
      logger.info("[OSS/DATA] PUT CREATED[201] {}", file)
      Created
    }
    call.respondText(status = status) { dataFolder.relativize(file).toString() }
  }

  fun Route.postOssData() = put("/{name...}") {
    val name = call.parameters.getAll("name") ?: throw Except.of(BadRequest, "Usage: POST /oss/data/{name...}")
    val file = dataFolder.asRoot(name)
    Files.createDirectories(file.parent)
    val exists = Files.exists(file)
    val status = if (exists) {
      logger.info("[OSS/DATA] PST EXISTED[202] {}", file)
      Accepted
    } else {
      call.request.receiveChannel().save(file)
      logger.info("[OSS/DATA] PST CREATED[201] {}", file)
      Created
    }
    call.respondText(status = status) { dataFolder.relativize(file).toString() }
  }

  fun Route.deleteOssData() = delete("/{name...}") {
    val name = call.parameters.getAll("name") ?: throw Except.of(BadRequest, "Usage: DELETE /oss/data/{name...}")
    val file = dataFolder.asRoot(name)
    val status = if (Files.exists(file)) {
      val count = file.deleteRecursive()
      logger.info("[OSS/DATA] DEL DELETED[200] {} -> {}", file, count)
      OK
    } else {
      logger.info("[OSS/DATA] DEL NOTHING[404] {}", file)
      NotFound
    }
    call.respondText(status = status) { dataFolder.relativize(file).toString() }
  }

  fun Route.headOssData() = head("/{name...}") {
    val name = call.parameters.getAll("name") ?: throw Except.of(BadRequest, "Usage: HEAD /oss/data/{name...}")
    val file = dataFolder.asRoot(name)
    val status = if (Files.exists(file)) {
      if (file.isDirectory()) {
        logger.info("[OSS/DATA] HED FOLDERS[403] {}", file)
        Forbidden
      } else {
        logger.info("[OSS/DATA] HED EXISTED[200] {}", file)
        OK
      }
    } else {
      logger.info("[OSS/DATA] HED NOTHING[404] {}", file)
      NotFound
    }
    call.respondText(status = status) { dataFolder.relativize(file).toString() }
  }

  fun Route.getOssData() = get("/{name...}") {
    val name = call.parameters.getAll("name") ?: throw Except.of(BadRequest, "Usage: GET /oss/data/{name...}")
    val file = dataFolder.asRoot(name)
    if (Files.exists(file)) {
      if (file.isDirectory()) {
        call.respondText(status = Forbidden) { dataFolder.relativize(file).toString() }
        logger.info("[OSS/DATA] GET FOLDERS[403] {}", file)
      } else {
        call.respondOutputStream(OctetStream, OK, file.fileSize()) {
          file.inputStream(StandardOpenOption.READ).transferTo(this)
        }
        logger.info("[OSS/DATA] GET EXISTED[200] {}", file)
      }
    } else {
      call.respondText(status = NotFound) { dataFolder.relativize(file).toString() }
      logger.info("[OSS/DATA] GET NOTHING[404] {}", file)
    }

  }

  private fun Path.deleteRecursive(): Int {
    var count = 0
    Files.list(this).forEach {
      if (it.isDirectory()) {
        count += it.deleteRecursive()
      } else {
        Files.delete(it)
        count++
      }
    }
    Files.delete(this)
    return count
  }

}