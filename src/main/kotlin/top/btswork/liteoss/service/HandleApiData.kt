package top.btswork.liteoss.service

import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import top.btswork.liteoss.auxiliary.listAllFile
import top.btswork.liteoss.dataFolder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory

object HandleApiData {

  fun Route.getApiDataSize() = get("/size") {
    call.respondText {
      val h = call.request.queryParameters.contains("h")
      var count = 0
      var total = 0L
      dataFolder.listAllFile().forEach {
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

  fun Route.getApiDataList() = get("/list") {
    call.respondText {
      val h = call.request.queryParameters.contains("h")
      val builder = StringBuilder()
      dataFolder.listAllFile().forEach {
        builder
          .append(if (h) String.format("%,d", it.fileSize()) else it.fileSize())
          .append(" ")
          .append(dataFolder.relativize(it).toString())
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

  fun Route.postApiDataCleanup() = post("/cleanup") {
    call.respondText {
      val list = dataFolder.recursiveCleanUp()
      list.joinToString(separator = "\n")
    }
  }

  private fun Path.recursiveCleanUp() = ArrayList<Path>().also { recursiveCleanUpActual(it) }

  private fun Path.recursiveCleanUpActual(list: MutableList<Path>) {
    val listFolder = Files.list(this).toList()
    if (listFolder.isEmpty()) {
      list.add(this)
      Files.delete(this)
    } else {
      listFolder.forEach {
        if (it.isDirectory()) {
          it.recursiveCleanUpActual(list)
        } else {
          return@forEach
        }
      }
    }
  }
}