plugins {
  id("nebula.release") version "16.1.0"
  id("org.openapi.generator") version "6.0.1" apply false
  id("org.jetbrains.kotlin.jvm") version "1.7.10" apply false
}

allprojects {
  repositories {
    mavenCentral()
  }

  group = "com.github.rahulsom"
}