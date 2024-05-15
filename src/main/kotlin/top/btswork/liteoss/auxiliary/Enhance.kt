package top.btswork.liteoss.auxiliary

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory

fun Path.asRoot(path: List<String>): Path = asRoot(*path.toTypedArray())
fun Path.asRoot(vararg path: String): Path = Paths.get(toString(), *path)

fun Path.listAllFile() = ArrayList<Path>().also { listAllFileRecursive(it) }
fun Path.listAllFileRecursive(list: MutableList<Path>) {
  Files.list(this).forEach {
    if (it.isDirectory()) {
      it.listAllFileRecursive(list)
    } else {
      list.add(it)
    }
  }
}

fun Array<String>.getConfig(default: String) =
  System.getenv("OSS_" + joinToString("_").uppercase())
    ?: System.getProperty(joinToString(".").lowercase())
    ?: default

