plugins {
  id("nebula.release") version "17.1.0"
  id("org.openapi.generator") version "6.2.1" apply false
  id("org.jetbrains.kotlin.jvm") version "1.7.21" apply false
}

allprojects {
  repositories {
    mavenCentral()
  }

  group = "com.github.rahulsom"
}