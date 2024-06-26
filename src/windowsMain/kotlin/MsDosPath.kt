import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.CreateDirectoryW
import platform.windows.CreateHardLinkA
import platform.windows.DeleteFileW

sealed interface MsDosPath {
	companion object {
		fun of(vararg path: String): MsDosPath =
			if (path.first().matches("^[a-zA-Z]:\\\\.*".toRegex())) MsDosAbsPath.of(*path)
			else MsDosRelPath.of(*path)

		@OptIn(ExperimentalForeignApi::class)
		val CWD get() = memScoped {
			val cstring: CPointer<ByteVar> = allocArray(512)
			if (getcwd(cstring, 512) == NULL)
				throw RuntimeException("could not get working directory")
			val string = cstring.toKString()
			return@memScoped of(string)
		}
	}

	val exists get() = access(pathString, F_OK) != -1
	val doesNotExist get() = !exists

	@OptIn(ExperimentalForeignApi::class)
	val isDirectory get() = exists && memScoped {
		val dp: CPointerVar<DIR> = alloc()
		dp.value = opendir(pathString)
		val isDir = dp.value != NULL
		closedir(dp.value)
		return@memScoped isDir
	}

	val isNotDirectory get() = !isDirectory

	@OptIn(ExperimentalForeignApi::class)
	fun ln(target: Path) {
		CreateHardLinkA(target.pathString, pathString, null)
	}

	fun rm() {
		DeleteFileW(pathString)
	}

	@OptIn(ExperimentalForeignApi::class)
	fun mkdir() {
		if (isNotDirectory && doesNotExist){
			if (parent.doesNotExist) parent.mkdir()
			CreateDirectoryW(pathString, null)
		}
	}

	@OptIn(ExperimentalForeignApi::class)
	fun resolveDirectoryEntries(): Set<MsDosPath> = memScoped {
		val dp: CPointerVar<DIR> = alloc()
		val ep: CPointerVar<dirent> = alloc()
		dp.value = opendir(pathString)
		if (dp.value == NULL)
			throw RuntimeException("$pathString is not a directory")
		val subPaths = mutableListOf<String>()
		ep.value = readdir(dp.value)
		while (ep.value != NULL) {
			subPaths += ep.pointed!!.d_name.toKString()
			ep.value = readdir(dp.value)
		}
		closedir(dp.value)
		subPaths.filterNot { it == "." || it == ".." }
			.map { resolve(it) }
			.sortedBy { it.name.replace("^\\.+".toRegex(), "") }
			.toSet()
	}

	fun resolve(string: String) = of(pathString, string)
	fun resolve(other: Path) = of(pathString, other.pathString)

	val name: String get() = pathString.split("\\\\".toRegex()).last()
	val pathString: String
	val absolute: MsDosPath
	val parent: MsDosPath
	val canonical: MsDosPath
}

private data class MsDosRelPath(val path: List<String>): MsDosPath {
	init { require(path.none { it.contains('\\') }) }
	companion object {
		fun of(vararg path: String) =
			path.flatMap { it.split("\\\\".toRegex()) }
        .filter { it.isNotEmpty() }
				.let(::MsDosRelPath)
	}

	override val pathString get() = path.joinToString("\\")
	override val absolute get() = MsDosPath.CWD.resolve(this)
	override val parent: MsDosPath get() = absolute.parent
	override val canonical: MsDosPath get() = absolute.canonical

	override fun toString() = pathString
}

private data class MsDosAbsPath(val path: List<String>, val drive: Char): MsDosPath {
	init { require(path.none { it.contains('\\') }) }
	companion object {
		fun of(vararg path: String) =
			path.flatMap { it.split("\\\\".toRegex()) }
        .filter { it.isNotEmpty() }
				.let { it.first() to it.drop(1) }
				.let { (drive, path) -> MsDosAbsPath(path, drive.first()) }
	}

	override val pathString get() = "$drive:" + path.joinToString("") { "\\$it" }
	override val absolute get() = this
	override val parent: MsDosPath get() =
		if (path.isEmpty()) this
		else MsDosAbsPath(path.dropLast(1), drive)
	override val canonical: MsDosPath get() =
		of(pathString
			.replace("\\", "/")
			.replace("[^/]+/\\.{2}/".toRegex(), "")
			.replace("/\\./".toRegex(), "/")
			.replace("[^/]+/\\.{2}$".toRegex(), "")
			.replace("/\\.$".toRegex(), "")
			.replace("/+".toRegex(), "/")
			.replace("/$".toRegex(), "")
			.replace("/", "\\"))

	override fun toString() = pathString
}
