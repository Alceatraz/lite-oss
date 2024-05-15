package top.btswork.liteoss.service

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import top.btswork.liteoss.blobFolder
import java.nio.file.Files
import kotlin.io.path.fileSize
import kotlin.io.path.name

object HandleApiBlob {

  fun Route.getApiBlobSize() = get("/size") {
    call.respondText {
      val h = call.request.queryParameters.contains("h")
      var count = 0
      var total = 0L
      Files.list(blobFolder).forEach {
        count += 1
        total += it.fileSize()
      }
      if (h) {
        "$count ${String.format("%,d", total)}"
      } else {
        "$count $total"
      }
    }
  }

  fun Route.getApiBlobList() = get("/list") {
    call.respondText {
      val h = call.request.queryParameters.contains("h")
      val builder = StringBuilder()
      Files.list(blobFolder).forEach {
        builder
          .append(it.name)
          .append(" ")
          .append(if (h) String.format("%,d", it.fileSize()) else it.fileSize())
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


}