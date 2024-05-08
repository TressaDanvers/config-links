import platform.posix.*

internal const val VERSION = "v0.1.0"

internal lateinit var target: PosixPath
internal lateinit var source: PosixPath

internal fun help() {
	println("""
		${ANSI_BOLD}Usage:${ANSI_NOSTRENGTH} config-links [options]
		${ANSI_BOLD}Options:${ANSI_NOSTRENGTH}
			-h, --help            Display this information.
			-s, --source <arg>    Specify source path.
			-t, --target <arg>    Specify target path.
			-v, --version         Display version information.
	""".trimIndent())
}

internal fun version() {
	println("${ANSI_BOLD}config-links${ANSI_NOSTRENGTH} $VERSION")
}

internal fun setTargetPath(arg: String) {
	target = PosixPath.of(arg)
}

internal fun setSourcePath(arg: String) {
	source = PosixPath.of(arg)
}