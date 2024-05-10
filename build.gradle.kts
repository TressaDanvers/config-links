plugins {
  kotlin("multiplatform") version "1.9.20-RC2"
}

group = "ch.protonmail.tdanvers"
version = "v0.1.0-pre"

repositories {
  mavenCentral()
}

kotlin {
  val hostOs = System.getProperty("os.name")
  val isArm64 = System.getProperty("os.arch") == "aarch64"
  val isMingwX64 = hostOs.startsWith("Windows")

  sourceSets { val nativeMain by creating { dependsOn(commonMain.get()) } }

  when {
    hostOs == "Linux" && isArm64 -> linuxArm64("linux") { sourceSets {
      val linuxMain by getting { dependsOn(nativeMain.get()) }
    } }
    hostOs == "Linux" && !isArm64 -> linuxX64("linux") { sourceSets {
      val linuxMain by getting { dependsOn(nativeMain.get()) }
    } }
    isMingwX64 -> mingwX64("windows") { sourceSets {
      val windowsMain by getting { dependsOn(nativeMain.get()) }
    } }
    else -> throw GradleException("Host OS is not supported.")
  }.apply { binaries { executable { entryPoint = "main" } } }
}
