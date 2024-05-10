import kotlinx.cinterop.*
import platform.posix.*

internal actual fun hostOs() = "Windows"

@OptIn(ExperimentalForeignApi::class)
internal actual fun home(): Path = Path(getenv("USERPROFILE")!!.toKString())

@OptIn(ExperimentalForeignApi::class)
internal actual fun dataHome(): Path = Path(getenv("APPDATA")!!.toKString())

internal actual fun sourceDefault(): Path =
  dataHome().resolve("ConfigLinks\\Windows")