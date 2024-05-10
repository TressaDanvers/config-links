internal const val VERSION = "v0.1.0"

internal var source: Path = sourceDefault()
internal var target: Path = home()

internal expect fun hostOs(): String
internal expect fun home(): Path
internal expect fun dataHome(): Path
internal expect fun sourceDefault(): Path

internal fun canonicalize() {
  source = source.canonical
  target = target.canonical
}

internal fun manage(file: String) = manage(Path(file))

internal fun manage(file: Path) {
  canonicalize()
  failIfFalse(file.exists, "\"${file.canonical}\" does not exist to manage")
  failIfFalse(isInTarget(file), "\"${file.canonical}\" is not in the target directory")
  info("now managing: \"${stripTarget(file)}\"")
  if (source.doesNotExist) {
    info("source does not exist, creating")
    source.mkdir()
  }
  if (file.isDirectory) file.linkAllTo(targetInSource(file))
  else file.ln(targetInSource(file))
}

internal fun linkAll() {
  canonicalize()
  info("linking config-files")
  if (source.exists) {
    if (target.doesNotExist) target.mkdir()
    failIfFalse(source.isDirectory && target.isDirectory,
      "source and/or target are not directories")
    source.linkAllTo(target)
  } else info("source directory does not exist, nothing to be done")
}

internal fun Path.linkAllTo(target: Path) {
	if (target.doesNotExist) target.mkdir()
	if (target.isNotDirectory) fail("file exists in place of target directory")

	resolveDirectoryEntries()
		.filter { it.isNotDirectory }
		.onEach { info("linking \"${stripBoth(it)}\"") }
		.onEach { it.ln(target.resolve(it.name)) }

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
			-m, --manage <arg>    Add the given file to managed files for this OS.
			-s, --source <arg>    Specify source path.
			                      default: "${sourceDefault()}"
			-t, --target <arg>    Specify target path.
			                      default: "${home()}"
			-v, --version         Display version information.
	""".trimIndent())
}

internal fun version() {
	println("${ANSI_BOLD}config-links${ANSI_NOSTRENGTH} $VERSION for ${hostOs()}")
}

internal fun setTargetPath(arg: String) {
	target = Path(arg)
}

internal fun setSourcePath(arg: String) {
	source = Path(arg)
}

private fun isInTarget(file: Path) =
  file.canonical.pathString.startsWith(target.pathString)

private fun targetInSource(file: Path) =
  Path(file.canonical.pathString.replace(target.pathString, source.pathString))

private fun sourceInTarget(file: Path) =
  Path(file.canonical.pathString.replace(source.pathString, target.pathString))

private fun stripBoth(file: Path) =
  file.canonical.pathString.replace(
    ("^(?:${source.pathString.replace("\\", "\\\\")}|" +
        "${target.pathString.replace("\\", "\\\\")})/?").toRegex(), "")


private fun stripTarget(file: Path) =
  file.canonical.pathString
    .replace("^${target.pathString.replace("\\", "\\\\")}/?".toRegex(), "")

private fun stripSource(file: Path) =
  file.canonical.pathString
    .replace("^${source.pathString.replace("\\", "\\\\")}/?".toRegex(), "")