package top.btswork.liteoss

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallFailed
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.btswork.liteoss.auxiliary.getConfig
import top.btswork.liteoss.common.Constant.HELP_ALL
import top.btswork.liteoss.common.Constant.HELP_API
import top.btswork.liteoss.common.Constant.HELP_BLOB
import top.btswork.liteoss.common.Constant.HELP_DATA
import top.btswork.liteoss.common.Constant.HELP_LINK
import top.btswork.liteoss.common.Constant.HELP_OSS
import top.btswork.liteoss.service.HandleApiBlob.getApiBlobList
import top.btswork.liteoss.service.HandleApiBlob.getApiBlobSize
import top.btswork.liteoss.service.HandleApiData.getApiDataList
import top.btswork.liteoss.service.HandleApiData.getApiDataSize
import top.btswork.liteoss.service.HandleApiData.postApiDataCleanup
import top.btswork.liteoss.service.HandleApiLink.getApiLinkList
import top.btswork.liteoss.service.HandleApiLink.postApiLinkCleanup
import top.btswork.liteoss.service.HandleOss.postOss
import top.btswork.liteoss.service.HandleOssBlob.deleteOssBlob
import top.btswork.liteoss.service.HandleOssBlob.getOssBlob
import top.btswork.liteoss.service.HandleOssBlob.headOssBlob
import top.btswork.liteoss.service.HandleOssBlob.putOssBlob
import top.btswork.liteoss.service.HandleOssData.deleteOssData
import top.btswork.liteoss.service.HandleOssData.getOssData
import top.btswork.liteoss.service.HandleOssData.headOssData
import top.btswork.liteoss.service.HandleOssData.postOssData
import top.btswork.liteoss.service.HandleOssData.putOssData
import top.btswork.liteoss.service.HandleOssLink.deleteOssLink
import top.btswork.liteoss.service.HandleOssLink.getOssLink
import top.btswork.liteoss.service.HandleOssLink.headOssLink
import top.btswork.liteoss.service.HandleOssLink.putOssLink
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute

val logger: Logger = LoggerFactory.getLogger("LiteOSS")

val linux: Boolean = System.getProperty("os.name").lowercase().contains("linux")

val blobPath = arrayOf("schema", "blob").getConfig("schema/blob")
val dataPath = arrayOf("schema", "data").getConfig("schema/data")
val linkPath = arrayOf("schema", "link").getConfig("schema/link")
val tempPath = arrayOf("schema", "temp").getConfig("schema/temp")

val serverHost = arrayOf("server", "host").getConfig("0.0.0.0")
val serverPort = arrayOf("server", "port").getConfig("80")

val enableLinkTrim = arrayOf("feature", "link", "trim").getConfig("false").toBoolean()

val blobFolder: Path = Paths.get(blobPath).absolute().normalize()
val dataFolder: Path = Paths.get(dataPath).absolute().normalize()
val linkFolder: Path = Paths.get(linkPath).absolute().normalize()
val tempFolder: Path = Paths.get(tempPath).absolute().normalize()


fun main(args: Array<String>) {

  if (args.contains("-h") || args.contains("--help")) {
    println(HELP_ALL)
    return
  }

  if (!linux) {
    logger.error("OS not linux, Link feature will throw exception when use")
  }

  logger.info("BlobPath set to {} -> {}", blobPath, blobFolder)
  logger.info("DataPath set to {} -> {}", dataPath, dataFolder)
  logger.info("LinkPath set to {} -> {}", linkPath, linkFolder)
  logger.info("TempPath set to {} -> {}", tempPath, tempFolder)

  logger.info("ServerHost set to -> {}", serverHost)
  logger.info("ServerPort set to -> {}", serverPort)

  logger.info("Feature RawLinkTrim -> {}", if (enableLinkTrim) "Enabled" else "Disable")

  Files.createDirectories(blobFolder)
  Files.createDirectories(dataFolder)
  Files.createDirectories(linkFolder)
  Files.createDirectories(tempFolder)

  Files.list(tempFolder).forEach(Files::delete)

  embeddedServer(CIO, host = serverHost, port = serverPort.toInt(), module = Application::module).start(true)

}

fun Application.module() {

  install(IgnoreTrailingSlash)
  install(ControllerExceptPlugin)

  routing {

    route("/api") {

      route("/blob") {
        getApiBlobSize()
        getApiBlobList()
      }

      route("data") {
        getApiDataSize()
        getApiDataList()
        postApiDataCleanup()
      }

      route("/link") {
        getApiLinkList()
        postApiLinkCleanup()
      }

      get { call.respondText { HELP_API } }

    }

    route("/oss") {

      route("/blob") {
        putOssBlob()
        getOssBlob()
        headOssBlob()
        deleteOssBlob()
        get { call.respondText { HELP_BLOB } }
      }

      route("/data") {
        putOssData()
        postOssData()
        deleteOssData()
        headOssData()
        getOssData()
        get { call.respondText { HELP_DATA } }
      }

      route("/link") {
        putOssLink()
        getOssLink()
        headOssLink()
        deleteOssLink()
        get { call.respondText { HELP_LINK } }
      }

      postOss()

      get { call.respondText { HELP_OSS } }

    }

    get("/") { call.respondText { HELP_ALL } }

  }

}

val ControllerExceptPlugin = createApplicationPlugin("ExceptionHandler") {
  on(CallFailed) { call, cause ->
    if (cause is Except) {
      call.respondText(status = cause.code) { cause.message ?: "" }
    } else {
      throw cause
    }
  }
}


class Except private constructor(val code: HttpStatusCode, message: String?) : RuntimeException(message) {
  companion object {
    fun of(code: HttpStatusCode, message: String? = "Except exception") = Except(code, message)
  }
}