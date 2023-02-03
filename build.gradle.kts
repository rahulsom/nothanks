plugins {
  id("nebula.release") version "17.1.0"
  id("org.openapi.generator") version "6.3.0" apply false
  id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
}

allprojects {
  repositories {
    mavenCentral()
  }

  group = "com.github.rahulsom"
}