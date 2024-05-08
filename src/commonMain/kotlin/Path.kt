@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

expect fun Path(vararg path: String): Path

expect interface Path {
  val name: String
  val pathString: String
  val absolute: Path

  val exists: Boolean
  val doesNotExist: Boolean
  val isDirectory: Boolean
  val isNotDirectory: Boolean

  fun mkdir()
  fun ln(target: Path)

  fun resolveDirectoryEntries(): Set<Path>
  fun resolve(string: String): Path
  fun resolve(other: Path): Path
}