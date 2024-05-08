internal const val VERSION = "v0.1.0"

internal lateinit var source: Path
internal lateinit var target: Path

internal fun linkAll() {
	require(source.isDirectory)
	require(target.isDirectory)
	source.linkAllTo(target)
}

internal fun Path.linkAllTo(target: Path) {
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
	target = Path(arg)
}

internal fun setSourcePath(arg: String) {
	source = Path(arg)
}