plugins {
  id("nebula.release") version "16.0.0"
  id("org.openapi.generator") version "5.3.0" apply false
  id("org.jetbrains.kotlin.jvm") version "1.6.10" apply false
}

allprojects {
  repositories {
    mavenCentral()
  }

  group = "com.github.rahulsom"
}