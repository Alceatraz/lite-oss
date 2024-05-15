package top.btswork.liteoss.service

import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.put
import top.btswork.liteoss.Except
import top.btswork.liteoss.auxiliary.asRoot
import top.btswork.liteoss.blobFolder
import top.btswork.liteoss.linkFolder
import top.btswork.liteoss.logger
import top.btswork.liteoss.service.Service.string
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.readSymbolicLink


object HandleOssLink {

  fun Route.putOssLink() = put("/{name}") {
    val name = call.parameters["name"] ?: throw Except.of(BadRequest, "USAGE: PUT /oss/link/{name}")
    val hash = call.request.receiveChannel().string()
    val blobFile = blobFolder.asRoot(hash)
    if (Files.notExists(blobFile)) {
      logger.info("[OSS/LINK] PUT NOTHING[404] {} -> {}", name, hash)
      throw Except.of(NotFound, "ERROR: Target file not exist -> $blobFile")
    }
    val linkFile = linkFolder.asRoot(name)
    val exists = Files.deleteIfExists(linkFile)
    val relative = linkFolder.relativize(blobFile)
    Files.createSymbolicLink(linkFile, relative)
    val status = if (exists) {
      logger.info("[OSS/LINK] PUT EXISTED[202] {} -> {}", name, hash)
      Accepted
    } else {
      logger.info("[OSS/LINK] PUT CREATED[201] {} -> {}", name, hash)
      Created
    }
    call.response.status(status)
  }

  fun Route.deleteOssLink() = delete("/{name}") {
    val name = call.parameters["name"] ?: throw Except.of(BadRequest, "USAGE: DELETE /oss/link/{name}")
    val linkFile = linkFolder.asRoot(name)
    val exist = Files.deleteIfExists(linkFile)
    val status = if (exist) {
      logger.info("[OSS/LINK] DEL DELETED[201] {}", name)
      OK
    } else {
      logger.info("[OSS/LINK] DEL NOTHING[404] {}", name)
      NotFound
    }
    call.response.status(status)
  }

  fun Route.headOssLink() = head("/{name}") {
    val name = call.parameters["name"] ?: throw Except.of(BadRequest, "USAGE: HEAD /oss/link/{name}")
    val linkFile = linkFolder.asRoot(name)
    val exist = Files.exists(linkFile)
    val status = if (exist) {
      logger.info("[OSS/LINK] HED EXISTED[200] {}", name)
      OK
    } else {
      logger.info("[OSS/LINK] HED NOTHING[404] {}", name)
      NotFound
    }
    call.response.status(status)
  }

  fun Route.getOssLink() = get("/{name}") {
    val name = call.parameters["name"] ?: throw Except.of(BadRequest, "USAGE: GET /oss/link/{name}")
    val linkFile = linkFolder.asRoot(name)
    if (Files.exists(linkFile).not()) {
      logger.info("[OSS/LINK] GET NOTHING[404] {} LINK", name)
      throw Except.of(NotFound, "ERROR: Link file not exist -> $name")
    }
    val hash = linkFile.readSymbolicLink().name
    val blobFile = blobFolder.asRoot(hash)
    if (Files.exists(blobFile).not()) {
      logger.info("[OSS/LINK] GET NOTHING[404] {} TARGET", name)
      throw Except.of(NotFound, "ERROR: Target file not exist -> $hash")
    }
    logger.info("LINK GET {} -> {}", name, hash)
    call.respondText(status = OK) { hash }
  }

}

