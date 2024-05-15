package top.btswork.liteoss.service

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import top.btswork.liteoss.auxiliary.asRoot
import top.btswork.liteoss.blobFolder
import top.btswork.liteoss.linkFolder
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readSymbolicLink

object HandleApiLink {

  fun Route.getApiLinkList() = get("/list") {
    call.respondText {
      val builder = StringBuilder()
      Files.list(linkFolder).forEach {
        builder
          .append(it.name)
          .append(" ")
          .append(it.readSymbolicLink().name)
          .append("\n")
      }
      if (builder.isEmpty()) {
        ""
      } else {
        builder.setLength(builder.length - 1)
        builder.toString()
      }
    }
  }

  fun Route.postApiLinkCleanup() = post("/cleanup") {
    call.respondText {
      val builder = StringBuilder()
      Files.list(linkFolder).forEach {
        val name = it.readSymbolicLink().name
        val blobFile = blobFolder.asRoot(name)
        if (blobFile.exists()) return@forEach
        builder.append(it.name)
          .append(" ")
          .append(name)
          .append("\n")
        Files.delete(it)
      }
      if (builder.isEmpty()) {
        ""
      } else {
        builder.setLength(builder.length - 1)
        builder.toString()
      }
    }
  }


}