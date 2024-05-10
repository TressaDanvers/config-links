internal const val ANSI_BOLD = "\u001B[1m"
internal const val ANSI_NOSTRENGTH = "\u001B[22m"

internal const val ANSI_RED = "\u001B[31m"
internal const val ANSI_YELLOW = "\u001B[33m"
internal const val ANSI_BLUE = "\u001B[34m"
internal const val ANSI_NOCOLOR = "\u001B[39m"

internal fun failIfFalse(value: Boolean, message: String) {
  if (!value) fail(message)
}

internal expect fun info(message: String)
internal expect fun warn(message: String)
internal expect fun err(message: String)
internal expect fun err(e: Throwable)
internal expect fun fail(message: String)
internal expect fun fail(e: Throwable)

sealed interface ExitStatus
data object ExitSuccess: ExitStatus
data object ExitFailure: ExitStatus

internal expect fun exit(status: ExitStatus)