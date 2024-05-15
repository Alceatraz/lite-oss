@file:Suppress("RedundantSuspendModifier")

package top.btswork.liteoss.service

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.btswork.liteoss.auxiliary.asRoot
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.WRITE
import java.security.MessageDigest
import java.util.UUID
import kotlin.io.path.outputStream

object Service {

  fun Path.newRandomFile(): Path {
    while (true) {
      val name = UUID.randomUUID().toString().replace("-", "")
      val path = asRoot(name)
      if (Files.notExists(path)) return path
    }
  }

  suspend fun ByteReadChannel.string(charset: Charset = Charsets.UTF_8): String {
    val stream = ByteArrayOutputStream()
    copyTo(stream)
    return stream.toString(charset)
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  suspend fun ByteReadChannel.save(path: Path) {
    Files.createFile(path)
    val outputStream = path.outputStream(WRITE)
    copyTo(outputStream)
    outputStream.flush()
    outputStream.close()
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  suspend fun ByteReadChannel.saveAndHash(path: Path): String {
    Files.createFile(path)
    val outputStream = path.outputStream(WRITE)
    val pipeline = SHA256Pipeline(outputStream)
    copyTo(pipeline)
    pipeline.flush()
    pipeline.close()
    return pipeline.digestHexString()
  }

  private class SHA256Pipeline(private val outputStream: OutputStream) : OutputStream() {

    private val digest = MessageDigest.getInstance("SHA-256")

    override fun close() = outputStream.close()
    override fun flush() = outputStream.flush()

    override fun write(b: Int) = throw NotImplementedError()
    override fun write(b: ByteArray) = throw NotImplementedError()
    override fun write(b: ByteArray, off: Int, len: Int) {
      digest.update(b, off, len)
      outputStream.write(b, off, len)
    }

    fun digestHexString() = BigInteger(1, digest.digest()).toString(16).padStart(64, '0')

  }

}


