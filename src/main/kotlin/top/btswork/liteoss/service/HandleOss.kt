package top.btswork.liteoss.service

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.btswork.liteoss.Except
import top.btswork.liteoss.auxiliary.asRoot
import top.btswork.liteoss.blobFolder
import top.btswork.liteoss.enableLinkTrim
import top.btswork.liteoss.linkFolder
import top.btswork.liteoss.logger
import top.btswork.liteoss.service.Service.newRandomFile
import top.btswork.liteoss.service.Service.saveAndHash
import top.btswork.liteoss.tempFolder
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import kotlin.io.path.name

object HandleOss {

  fun Route.postOss() = post("/{name}") {
    val name = call.parameters["name"] ?: throw Except.of(BadRequest, "USAGE: POST /oss/{name}")
    val tempFile = tempFolder.newRandomFile()
    val blobHash = call.request.receiveChannel().saveAndHash(tempFile)
    val blobFile = blobFolder.asRoot(blobHash)
    if (Files.exists(blobFile)) {
      Files.delete(tempFile)
      logger.info("[OSS/POST] BLOB EXISTED {}", blobHash)
    } else {
      Files.move(tempFile, blobFile, ATOMIC_MOVE)
      logger.info("[OSS/POST] BLOB CREATED {}", blobHash)
    }
    val linkFile = linkFolder.asRoot(name)
    Files.deleteIfExists(linkFile)
    val relative = linkFolder.relativize(blobFile)
    Files.createSymbolicLink(linkFile, relative)
    logger.info("[OSS/POST] LINK ENSURED {} -> {}", blobHash, name)
    if (enableLinkTrim) launch(Dispatchers.IO) {
      val a = System.nanoTime()
      Files.list(linkFolder)
        .filter { it.name.startsWith(blobHash) }
        .forEach { Files.delete(it) }
      val b = System.nanoTime()
      logger.info("[OSS/POST] LINK CLEANUP {} costs:{}", blobHash, b - a)
    }
    call.respondText(status = OK) { blobHash }
  }
}
