plugins {
  id("org.jetbrains.kotlin.jvm")
  id("com.github.johnrengelman.shadow") version "7.1.2"
  application
}

dependencies {
  implementation(project(":nothanks-client"))
  implementation("info.picocli:picocli:4.7.0")
  implementation("org.slf4j:slf4j-api:2.0.4")
  implementation("org.fusesource.jansi:jansi:2.4.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

  runtimeOnly("ch.qos.logback:logback-classic:1.4.4")

}

application {
  mainClass.set("com.github.rahulsom.nothanks.cli.MainKt")
}