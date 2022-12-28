plugins {
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.kapt")
  id("org.jetbrains.kotlin.plugin.allopen") version "1.8.0"
  id("org.jetbrains.kotlin.plugin.jpa") version "1.8.0"
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("io.micronaut.application") version "3.6.7"
  id("com.google.cloud.tools.jib") version "3.3.1"
}

val kotlinVersion = project.properties.get("kotlinVersion")

micronaut {
  runtime("netty")
  testRuntime("junit5")
  processing {
    incremental(true)
    annotations("com.github.rahulsom.nothanks.*")
  }
}

dependencies {
  kapt("io.micronaut:micronaut-http-validation")
  kapt("io.micronaut.data:micronaut-data-processor")
  kapt("io.micronaut.openapi:micronaut-openapi")
  kapt("io.micronaut.security:micronaut-security-annotations")

  implementation("io.micronaut:micronaut-http-client")
  implementation("io.micronaut:micronaut-runtime")
  implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
  implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
  implementation("io.micronaut.reactor:micronaut-reactor")
  implementation("io.micronaut.reactor:micronaut-reactor-http-client")
  implementation("io.micronaut.security:micronaut-security")
  implementation("io.micronaut.sql:micronaut-jdbc-hikari")
  implementation("io.swagger.core.v3:swagger-annotations")
  implementation("jakarta.annotation:jakarta.annotation-api")
  implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
  implementation("io.micronaut:micronaut-validation")
  implementation("io.micronaut.liquibase:micronaut-liquibase")

  runtimeOnly("ch.qos.logback:logback-classic")
  runtimeOnly("com.mattbertolini:liquibase-slf4j:4.1.0")
  runtimeOnly("org.postgresql:postgresql")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  compileOnly("org.graalvm.nativeimage:svm")

  testImplementation("org.assertj:assertj-core")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:testcontainers")

  testRuntimeOnly("com.h2database:h2:2.1.214")
}

application {
  mainClass.set("com.github.rahulsom.nothanks.ApplicationKt")
}
java {
  sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = "11"
    }
  }
  compileTestKotlin {
    kotlinOptions {
      jvmTarget = "11"
    }
  }
  jib {
    to {
      image = "gcr.io/myapp/jib-image"
    }
  }
}

tasks.run.configure {
  environment.putAll(
    mapOf(
      "JDBC_DATABASE_URL" to "jdbc:postgresql://localhost:5432/postgres",
      "JDBC_DATABASE_USERNAME" to "postgres",
      "JDBC_DATABASE_PASSWORD" to "mysecretpassword",
    )
  )
}

kapt {
  arguments {
    arg("micronaut.openapi.views.spec", "redoc.enabled=true,rapidoc.enabled=true,swagger-ui.enabled=true,swagger-ui.theme=flattop")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}