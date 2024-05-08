import kotlinx.cinterop.*
import platform.posix.*

internal const val ANSI_BOLD = "\u001B[1m"
internal const val ANSI_NOSTRENGTH = "\u001B[22m"

internal const val ANSI_RED = "\u001B[31m"
internal const val ANSI_YELLOW = "\u001B[33m"
internal const val ANSI_BLUE = "\u001B[34m"
internal const val ANSI_NOCOLOR = "\u001B[39m"

@OptIn(ExperimentalForeignApi::class)
internal val STDERR = fdopen(2, "w")

internal fun info(message: String) {
	println("${ANSI_BOLD}config-links: ${ANSI_BLUE}info: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}${message}")
}

@OptIn(ExperimentalForeignApi::class)
internal fun warn(message: String) {
	fprintf(STDERR, "${ANSI_BOLD}config-links: ${ANSI_YELLOW}warning: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}%s\n", message)
	fflush(STDERR)
}

@OptIn(ExperimentalForeignApi::class)
internal fun err(message: String) {
	fprintf(STDERR, "${ANSI_BOLD}config-links: ${ANSI_RED}error: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}%s\n", message)
	fflush(STDERR)
}

internal fun err(e: Throwable) {
	err(e.message ?: "runtime error thrown")
}

@OptIn(ExperimentalForeignApi::class)
internal fun fail(message: String) {
	fprintf(STDERR, "${ANSI_BOLD}config-links: ${ANSI_RED}fatal error: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}%s\n", message)
	fflush(STDERR)
	exit(EXIT_FAILURE)
}

internal fun fail(e: Throwable) {
	fail(e.message ?: "runtime error thrown")
}