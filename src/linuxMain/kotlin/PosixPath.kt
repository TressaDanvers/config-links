import kotlinx.cinterop.*
import platform.posix.*

sealed interface PosixPath {
	companion object {
		fun of(vararg path: String): PosixPath =
			if (path.first().startsWith('/')) PosixAbsPath.of(*path)
			else PosixRelPath.of(*path)

		@OptIn(ExperimentalForeignApi::class)
		val CWD get() = memScoped {
			val cstring: CPointer<ByteVar> = allocArray(512)
			if (getcwd(cstring, 512u) == NULL)
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

	fun ln(target: Path) {
		link(pathString, target.pathString)
	}

	fun mkdir() {
		if (isNotDirectory && doesNotExist)
			mkdir(pathString, 0b111111111u)
	}

	@OptIn(ExperimentalForeignApi::class)
	fun resolveDirectoryEntries(): Set<PosixPath> = memScoped {
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

	val name: String get() = pathString.split("/".toRegex()).lastOrNull() ?: "/"
	val pathString: String
	val absolute: PosixPath
}

private data class PosixRelPath(val path: List<String>): PosixPath {
	init { require(path.none { it.contains('/') }) }
	companion object {
		fun of(vararg path: String) =
			path.flatMap { it.split("/".toRegex()) }
				.also { require(it.none { st -> st.isBlank() }) }
				.let(::PosixRelPath)
	}

	override val pathString get() = path.joinToString("/")
	override val absolute get() = PosixPath.CWD.resolve(this)

	override fun toString() = pathString
}

private data class PosixAbsPath(val path: List<String>): PosixPath {
	init { require(path.none { it.contains('/') }) }
	companion object {
		fun of(vararg path: String) =
			path.flatMap { it.split("/".toRegex()) }
				.also { require(it.none { st -> st.isBlank() }) }
				.let(::PosixAbsPath)
	}

	override val pathString get() = path.joinToString("") { "/$it" }
	override val absolute get() = this

	override fun toString() = pathString
}
