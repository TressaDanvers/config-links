@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

actual fun Path(vararg path: String): Path = MsDosPath.of(*path)

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias Path = MsDosPath