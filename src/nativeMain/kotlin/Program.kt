internal const val VERSION = "v0.1.0"

internal lateinit var source: PosixPath
internal lateinit var target: PosixPath

internal fun linkAll() {
	require(source.isDirectory)
	require(target.isDirectory)
	source.linkAllTo(target)
}

internal fun PosixPath.linkAllTo(target: PosixPath) {
	info("linking all in $this to $target")

	if (target.doesNotExist) target.mkdir()
	if (target.isNotDirectory) fail("file exists in place of target directory")

	resolveDirectoryEntries()
		.filter { it.isNotDirectory }
		.onEach { info("linking $it") }
		.onEach { if (target.resolve(it.name).doesNotExist) it.ln(target.resolve(it.name)) }

	resolveDirectoryEntries()
		.filter { it.isDirectory }
		.onEach { it.linkAllTo(target.resolve(it.name)) }
}

internal fun help() {
	println("""
		${ANSI_BOLD}Usage:${ANSI_NOSTRENGTH} config-links [options]
		${ANSI_BOLD}Options:${ANSI_NOSTRENGTH}
			-h, --help            Display this information.
			    --link            Link all files in source to target.
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