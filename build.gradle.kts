plugins {
  kotlin("multiplatform") version "1.9.20-RC2"
}

group = "ch.protonmail.tdanvers"
version = "v0.1.0"

repositories {
  mavenCentral()
}

kotlin {
  val hostOs = System.getProperty("os.name")
  val isArm64 = System.getProperty("os.arch") == "aarch64"
  val isMingwX64 = hostOs.startsWith("Windows")

  when {
    hostOs == "Linux" && isArm64 -> linuxArm64("linuxNative")
    hostOs == "Linux" && !isArm64 -> linuxX64("linuxNative")
    isMingwX64 -> mingwX64("winNative")
    else -> throw GradleException("Host OS is not supported.")
  }.apply { binaries { executable { entryPoint = "main" } } }
}
