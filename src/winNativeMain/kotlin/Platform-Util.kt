import kotlinx.cinterop.*
import platform.posix.*

@OptIn(ExperimentalForeignApi::class)
internal val STDERR = fdopen(2, "w")

internal actual fun info(message: String) {
	println("${ANSI_BOLD}config-links: ${ANSI_BLUE}info: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}${message}")
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun warn(message: String) {
	fprintf(STDERR, "${ANSI_BOLD}config-links: ${ANSI_YELLOW}warning: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}%s\n", message)
	fflush(STDERR)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun err(message: String) {
	fprintf(STDERR, "${ANSI_BOLD}config-links: ${ANSI_RED}error: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}%s\n", message)
	fflush(STDERR)
}

internal actual fun err(e: Throwable) {
	err(e.message ?: "runtime error thrown")
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun fail(message: String) {
	fprintf(STDERR, "${ANSI_BOLD}config-links: ${ANSI_RED}fatal error: ${ANSI_NOCOLOR + ANSI_NOSTRENGTH}%s\n", message)
	fflush(STDERR)
	exit(EXIT_FAILURE)
}

internal actual fun fail(e: Throwable) {
	fail(e.message ?: "runtime error thrown")
}