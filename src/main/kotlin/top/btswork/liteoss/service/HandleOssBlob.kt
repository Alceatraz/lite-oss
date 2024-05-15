package top.btswork.liteoss.service

import io.ktor.http.ContentType.Application.OctetStream
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
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
import top.btswork.liteoss.blobFolder
import top.btswork.liteoss.logger
import top.btswork.liteoss.service.Service.newRandomFile
import top.btswork.liteoss.service.Service.saveAndHash
import top.btswork.liteoss.tempFolder
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream

object HandleOssBlob {

  fun Route.putOssBlob() = put {
    val tempFile = tempFolder.newRandomFile()
    val blobHash = call.request.receiveChannel().saveAndHash(tempFile)
    val blobFile = blobFolder.asRoot(blobHash)
    val exists = Files.deleteIfExists(blobFile)
    Files.move(tempFile, blobFile, ATOMIC_MOVE)
    val status = if (exists) {
      logger.info("[OSS/BLOB] PUT EXISTED[202] {}", blobHash)
      Accepted
    } else {
      logger.info("[OSS/BLOB] PUT CREATED[201] {}", blobHash)
      Created
    }
    call.respondText(status = status) { blobHash }
  }

  fun Route.deleteOssBlob() = delete("/{hash}") {
    val hash = call.parameters["hash"] ?: throw Except.of(BadRequest, "Usage: DELETE /oss/blob/{hash}")
    val blobFile = blobFolder.asRoot(hash)
    val exist = Files.deleteIfExists(blobFile)
    val status = if (exist) {
      logger.info("[OSS/BLOB] DEL DELETED[200] {}", hash)
      OK
    } else {
      logger.info("[OSS/BLOB] PST NOTHING[404] {}", hash)
      NotFound
    }
    call.respondText(status = status) { hash }
  }

  fun Route.headOssBlob() = head("/{hash}") {
    val hash = call.parameters["hash"] ?: throw Except.of(BadRequest, "Usage: HEAD /oss/blob/{hash}")
    val blobFile = blobFolder.asRoot(hash)
    val exist = Files.exists(blobFile)
    val status = if (exist) {
      logger.info("[OSS/BLOB] HED EXISTED[200] {}", hash)
      OK
    } else {
      logger.info("[OSS/BLOB] HED NOTHING[404] {}", hash)
      NotFound
    }
    call.respondText(status = status) { hash }
  }

  fun Route.getOssBlob() = get("/{hash}") {
    val hash = call.parameters["hash"] ?: throw Except.of(BadRequest, "Usage: GET /oss/blob/{hash}")
    val blobFile = blobFolder.asRoot(hash)
    val exists = Files.exists(blobFile)
    if (exists) {
      logger.info("[OSS/BLOB] GET SENDING[200] {}", hash)
      call.respondOutputStream(OctetStream, OK, blobFile.fileSize()) {
        blobFile.inputStream(StandardOpenOption.READ).transferTo(this)
      }
    } else {
      logger.info("[OSS/BLOB] GET NOTHING[404] {}", hash)
      call.respondText(status = NotFound) { hash }
    }
  }
}