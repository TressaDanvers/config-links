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
				throw Error("could not get working directory")
			val string = cstring.toKString()
			free(cstring)
			return@memScoped of(string)
		}
	}

	fun resolve(string: String) = of(pathString, string)
	fun resolve(other: PosixPath) = of(pathString, other.pathString)

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
				.let(::PosixRelPath)
	}

	override val pathString get() = path.joinToString("") { "/$it" }
	override val absolute get() = this

	override fun toString() = pathString
}
