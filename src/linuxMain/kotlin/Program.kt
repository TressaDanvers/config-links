import kotlinx.cinterop.*
import platform.posix.*

internal actual fun hostOs() = "linux"

@OptIn(ExperimentalForeignApi::class)
internal actual fun home(): Path = Path(getenv("HOME")!!.toKString())

@OptIn(ExperimentalForeignApi::class)
internal actual fun dataHome(): Path =
  getenv("XDG_DATA_HOME")?.toKString()?.let { Path(it) } ?:
  home().resolve(".local/share")

internal actual fun sourceDefault(): Path =
  dataHome().resolve("config-links/linux")